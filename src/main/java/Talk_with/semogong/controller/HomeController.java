package Talk_with.semogong.controller;

import Talk_with.semogong.configuration.SessionConst;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Goal;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.domain.dto.PostViewDto;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final MemberService memberService;

    @ModelAttribute("check")
    public boolean check(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId) {
        return loginMemberId != null;
    }

    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!");
    }

    @RequestMapping("/")
    public String home_page(@RequestParam(name = "page", defaultValue = "1") Integer page, @RequestParam(name = "focus", defaultValue = "all") String focus,
                            @ModelAttribute(name = "searchForm") SearchForm searchForm,
                            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId, Model model) {
        log.info("paging home");
        List<Post> posts;
        if (focus.equals("today")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(dateTimeFormatter);
            if (now.getHour() < 4) {
                date = now.minusDays(1).format(dateTimeFormatter);
            }
            if (searchForm.getSelected() == null) {
                posts = postService.getTodayPosts(date, (page - 1) * 12);
            } else {
                posts = postService.findByTodaySearch(searchForm.getSelected(), searchForm.getContent(), date, (page - 1) * 12);
            }
        } else if (focus.equals("my-posts")) {
            if (searchForm.getSelected() == null) {
                posts = postService.getMemberPosts(loginMemberId, (page - 1) * 12);
            } else {
                posts = postService.findBySearchMy(loginMemberId, searchForm.getSelected(), searchForm.getContent(), (page - 1) * 12);
            }
        } else {
            if (searchForm.getSelected() == null) {
                posts = postService.findByPage((page - 1) * 12);
            } else {
                posts = postService.findBySearch(searchForm.getSelected(), searchForm.getContent(), (page - 1) * 12);
            }
        }

        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        List<PostViewDto> postModals = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        Set<Long> ids = postModals.stream().map(PostViewDto::getId).collect(Collectors.toSet());

        List<Member> members = memberService.findAll();
        List<Member> bySearchMembers = memberService.findAll();;
        if (focus.equals("all-members") && searchForm.getSelected() != null) {
            bySearchMembers = memberService.findBySearch(searchForm.getSelected(), searchForm.getContent());
        }
        List<MemberDto> allMembers = members.stream().map(MemberDto::new).collect(Collectors.toList());
        List<MemberDto> memberDtos = new ArrayList<>();
        for (Member member : bySearchMembers) {
            MemberDto memberDto = new MemberDto(member);
            memberDto.setTotalTime(getAllTimes(member));
            memberDtos.add(memberDto);
        }

        if (loginMemberId != null) {
            Member loginMember = memberService.findOne(loginMemberId);
            MemberDto memberDto = new MemberDto(loginMember);
            model.addAttribute("member", memberDto);
            Optional<Post> optionalPost = postService.getRecentPost(loginMember.getId());
            PostViewDto memberRecentPostDto = optionalPost.map(PostViewDto::new).orElse(null);

            if ((loginMember.getState() == StudyState.STUDYING || loginMember.getState() == StudyState.BREAKING) & memberRecentPostDto != null) {
                model.addAttribute("recentPost", memberRecentPostDto);
                if (!ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }

            Times totalStudyTimes = null;
            Long focusedPostId = null;
            if (memberRecentPostDto != null) {
                totalStudyTimes = getTotalStudyTimes(memberRecentPostDto);
                focusedPostId = memberRecentPostDto.getId();
            }
            model.addAttribute("sTime", totalStudyTimes);
            model.addAttribute("focusedId", focusedPostId);
        }


        for (MemberDto m : allMembers) {
            Optional<Post> eachOptionalPost = postService.getRecentPost(m.getId());
            PostViewDto memberRecentPostDto = eachOptionalPost.map(PostViewDto::new).orElse(null);
            if (memberRecentPostDto != null) {
                m.setTime(getTotalStudyTimes(memberRecentPostDto));
            } else {
                m.setTime(null);
            }
        }

        List<MemberDto> memberRanking = new ArrayList<>(allMembers);
        memberRanking.sort(new TimeSorter());


        model.addAttribute("page", page);
        model.addAttribute("posts", postDtos);
        model.addAttribute("postModals", postModals);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("allMembers", allMembers);
        model.addAttribute("members", memberDtos);
        model.addAttribute("rankings", memberRanking.subList(0, 3));
        model.addAttribute("nav", "home");
        model.addAttribute("nav2", focus);

        return "home";
    }

    // 회원 총 학습 시간
    @ResponseBody
    @GetMapping("/members/times/{id}")
    public Times times(@PathVariable("id") Long id) {
        Optional<Post> optionalPost = postService.getRecentPost(id);
        PostViewDto memberRecentPostDto = optionalPost.map(PostViewDto::new).orElse(null);
        Times totalStudyTimes = new Times(0);
        if (memberRecentPostDto != null) {
            totalStudyTimes = getTotalStudyTimes(memberRecentPostDto);
        }
        return totalStudyTimes;
    }

    @Data
    static class SearchForm {

        private String selected;
        private String content;

    }

    private Times getAllTimes(Member member) {
        List<Post> posts = member.getPosts();
        int total = 0;
        for (Post post : posts) {
            if (post.getTimes().size() % 2 != 0) continue;
            Times times = getTimes(post.getTimes());
            total += times.getHour() * 60 + times.getMin();
        }

        return new Times(total);
    }

    private Times getTotalStudyTimes(PostViewDto memberRecentPostDto) {

        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDateTime createDateTime = memberRecentPostDto.getCreateTime();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH");
        LocalDate createDate = createDateTime.toLocalDate();
        LocalDate nowDate = nowDateTime.toLocalDate();
        int nowTime = Integer.parseInt(nowDateTime.format(timeFormatter));
        int createTime = Integer.parseInt(createDateTime.format(timeFormatter));
        Period period = Period.between(createDate, nowDate);
        List<String> times = memberRecentPostDto.getTimes();
        Times resultTime = getTimes(nowDateTime, timeFormatter, times);

        if (nowTime > 3 & nowTime < 24) {
            if (createDate.isEqual(nowDate) & createTime > 3) {
                return resultTime;
            }
        } else {
            if (nowDate.isEqual(createDate)) {
                return resultTime;
            }
            // 그 전날 작성한 글이 존재하는 지 확인
            else if (period.getDays() == 1 & createTime > 3) {
                return resultTime;
            }
        }


        return null;
    }

    private Times getTimes(LocalDateTime nowDateTime, DateTimeFormatter timeFormatter, List<String> times) {
        Times resultTime;
        if (times.size() % 2 == 0) {
            int total1 = 0;
            for (int i = 1; i < times.size(); i += 2) {
                String[] ends = times.get(i).split(":");
                String[] starts = times.get(i - 1).split(":");
                int endHour = Integer.parseInt(ends[0]);
                if (0 <= endHour & endHour < 4) {
                    endHour += 24;
                }
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;


                int startHour = Integer.parseInt(starts[0]);
                if (0 <= startHour & startHour < 4) {
                    startHour += 24;
                }
                int startMin = Integer.parseInt(starts[1]);
                int start = startHour * 60 + startMin;

                total1 += (end - start);
            }
            resultTime = new Times(total1);
        } else {
            int total2 = 0;
            for (int i = 1; i < times.size(); i += 2) {
                String[] ends = times.get(i).split(":");
                String[] starts = times.get(i - 1).split(":");
                int endHour = Integer.parseInt(ends[0]);
                if (0 <= endHour & endHour < 4) {
                    endHour += 24;
                }
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;

                int startHour = Integer.parseInt(starts[0]);
                if (0 <= startHour & startHour < 4) {
                    startHour += 24;
                }
                int startMin = Integer.parseInt(starts[1]);
                int start = startHour * 60 + startMin;

                total2 += (end - start);
            }

            DateTimeFormatter timeFormatter2 = DateTimeFormatter.ofPattern("mm");
            String[] starts = times.get(times.size() - 1).split(":");

            int endHour = Integer.parseInt(nowDateTime.format(timeFormatter));
            if (0 <= endHour & endHour < 4) {
                endHour += 24;
            }
            int endMin = Integer.parseInt(nowDateTime.format(timeFormatter2));
            int end = endHour * 60 + endMin;

            int startHour = Integer.parseInt(starts[0]);
            if (0 <= startHour & startHour < 4) {
                startHour += 24;
            }
            int startMin = Integer.parseInt(starts[1]);
            int start = startHour * 60 + startMin;

            total2 += (end - start);
            resultTime = new Times(total2);
        }
        return resultTime;
    }

    private Times getTimes(List<String> times) {
        Times resultTime = null;
        if (times.size() % 2 == 0) {
            int total1 = 0;
            for (int i = 1; i < times.size(); i += 2) {
                String[] ends = times.get(i).split(":");
                String[] starts = times.get(i - 1).split(":");
                int endHour = Integer.parseInt(ends[0]);
                if (0 <= endHour & endHour < 4) {
                    endHour += 24;
                }
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;


                int startHour = Integer.parseInt(starts[0]);
                if (0 <= startHour & startHour < 4) {
                    startHour += 24;
                }
                int startMin = Integer.parseInt(starts[1]);
                int start = startHour * 60 + startMin;

                total1 += (end - start);
            }
            resultTime = new Times(total1);
        }
        return resultTime;
    }


}
