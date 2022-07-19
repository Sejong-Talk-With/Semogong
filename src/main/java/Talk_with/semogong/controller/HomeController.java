package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.domain.dto.PostViewDto;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
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
    public boolean check(@SessionAttribute(name = "loginMember", required = false) Long loginMemberId) {
        return loginMemberId != null;
    }

    @RequestMapping({"/", "/{page}"})
    public String home_page(@PathVariable(name = "page", required = false) Integer page, @SessionAttribute(name = "loginMember", required = false) Long loginMemberId, Model model) {
        log.info("paging home");
        if (page == null) page = 1;
        List<Post> posts = postService.findByPage((page - 1) * 12);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        List<PostViewDto> postModals = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        Set<Long> ids = postModals.stream().map(PostViewDto::getId).collect(Collectors.toSet());
        List<Member> members = memberService.findAll();
        List<MemberDto> memberDtos = members.stream().map(MemberDto::new).collect(Collectors.toList());

        if (loginMemberId != null) {
            Member loginMember = memberService.findOne(loginMemberId);
            MemberForm memberForm = createMemberForm(loginMember);
            model.addAttribute("member", memberForm);
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


        for (MemberDto m : memberDtos) {
            Optional<Post> eachOptionalPost = postService.getRecentPost(m.getId());
            PostViewDto memberRecentPostDto = eachOptionalPost.map(PostViewDto::new).orElse(null);
            if (memberRecentPostDto != null) {
                m.setTime(getTotalStudyTimes(memberRecentPostDto));
            } else {
                m.setTime(null);
            }
        }

        List<MemberDto> memberRanking = new ArrayList<>(memberDtos);
        memberRanking.sort(new TimeSorter());


        model.addAttribute("page", page);
        model.addAttribute("posts", postDtos);
        model.addAttribute("postModals", postModals);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("allMembers", memberDtos);
        model.addAttribute("rankings", memberRanking.subList(0, 3));
        model.addAttribute("nav", "home");


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

    private MemberForm createMemberForm(Member member) {
        MemberForm memberForm = new MemberForm();
        memberForm.setId(member.getId());
        memberForm.setLoginId(member.getLoginId());
        memberForm.setName(member.getName());
        memberForm.setNickname(member.getNickname());
        memberForm.setDesiredJob(member.getDesiredJob());
        memberForm.setIntroduce(member.getIntroduce());
        memberForm.setState(member.getState());
        memberForm.setImage(member.getImage());
        return memberForm;
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


}
