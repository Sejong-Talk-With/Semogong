package Talk_with.semogong.controller;

import Talk_with.semogong.domain.*;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import Talk_with.semogong.service.S3Service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.FormatStyle.LONG;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final MemberService memberService;
    private final S3Service s3Service;

    @GetMapping("/data")
    public String data(Model model, Authentication authentication) throws IOException {
        if (authentication == null) {
            return "redirect:/";
        }
        model.addAttribute("nav", "data");
        model.addAttribute("check", true);
        model.addAttribute("plot", s3Service.load());
        return "analysis";
    }



    @RequestMapping("/")
    public String home(Model model, Authentication authentication) throws IOException {

        log.info("opened home");

        List<Post> posts = postService.findByPage(0);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        List<PostViewDto> postModals = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        Set<Long> ids = postModals.stream().map(PostViewDto::getId).collect(Collectors.toSet());

        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);

            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            Optional<Post> optionalPost = postService.getRecentPost(member.getId());
            PostViewDto memberRecentPostDto = optionalPost.map(PostViewDto::new).orElse(null);

            if ((member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) & memberRecentPostDto != null) {
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }

            Times totalStudyTimes = null;
            if (memberRecentPostDto != null) {
                totalStudyTimes = getTotalStudyTimes(memberRecentPostDto);
            }
            model.addAttribute("sTime", totalStudyTimes);

            List<Member> members = memberService.findAll();
            List<MemberDto> memberDtos = members.stream().map(MemberDto::new).collect(Collectors.toList());
            model.addAttribute("allMembers", memberDtos);

        } else {
            model.addAttribute("check", false);
        }

        model.addAttribute("page", -1);
        model.addAttribute("posts", postDtos);
        model.addAttribute("postModals", postModals);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("plot", s3Service.load());
        model.addAttribute("nav", "home");


        return "home";
    }

    @RequestMapping("/{page}")
    public String home_page(@PathVariable("page") Integer page, Model model, Authentication authentication) throws IOException {
        log.info("paging home");

        List<Post> posts = postService.findByPage((page - 1) * 12);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        List<PostViewDto> postModals = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        Set<Long> ids = postModals.stream().map(PostViewDto::getId).collect(Collectors.toSet());

        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);
            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            Optional<Post> optionalPost = postService.getRecentPost(member.getId());
            PostViewDto memberRecentPostDto = optionalPost.map(PostViewDto::new).orElse(null);

            if ((member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) & memberRecentPostDto != null) {
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }

            Times totalStudyTimes = null;
            if (memberRecentPostDto != null) {
                totalStudyTimes = getTotalStudyTimes(memberRecentPostDto);
            }
            model.addAttribute("sTime", totalStudyTimes);
            List<Member> members = memberService.findAll();
            List<MemberDto> memberDtos = members.stream().map(MemberDto::new).collect(Collectors.toList());
            model.addAttribute("allMembers", memberDtos);
        } else {
            model.addAttribute("check", false);
        }

        model.addAttribute("page", page);
        model.addAttribute("posts", postDtos);
        model.addAttribute("postModals", postModals);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("plot", s3Service.load());
        model.addAttribute("nav", "home");


        return "home";
    }



    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "exception", required = false) String exception, Model model, Authentication authentication) throws IOException {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        log.info("Login Error");
        List<Post> posts = postService.findByPage(0);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        List<PostViewDto> postModals = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        Set<Long> ids = postModals.stream().map(PostViewDto::getId).collect(Collectors.toSet());

        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);

            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            Optional<Post> optionalPost = postService.getRecentPost(member.getId());
            PostViewDto memberRecentPostDto = optionalPost.map(PostViewDto::new).orElse(null);

            if ((member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) & memberRecentPostDto != null) {
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }

            Times totalStudyTimes = null;
            if (memberRecentPostDto != null) {
                totalStudyTimes = getTotalStudyTimes(memberRecentPostDto);
            }
            model.addAttribute("sTime", totalStudyTimes);
            List<Member> members = memberService.findAll();
            List<MemberDto> memberDtos = members.stream().map(MemberDto::new).collect(Collectors.toList());
            model.addAttribute("allMembers", memberDtos);
        } else {
            model.addAttribute("check", false);
        }

        model.addAttribute("page", -1);
        model.addAttribute("posts", postDtos);
        model.addAttribute("postModals", postModals);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("plot", s3Service.load());
        model.addAttribute("nav", "home");

        return "home";
    }

    private Member getLoginMember(Authentication authentication) {
        MyUserDetail userDetail = (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        return memberService.findByLoginId(loginId); // "박승일"로 로그인 했다고 가정, 해당 로그인된 회원의 ID를 가져옴
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
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;

                int startHour = Integer.parseInt(starts[0]);
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
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;

                int startHour = Integer.parseInt(starts[0]);
                int startMin = Integer.parseInt(starts[1]);
                int start = startHour * 60 + startMin;

                total2 += (end - start);
            }

            DateTimeFormatter timeFormatter2 = DateTimeFormatter.ofPattern("mm");
            String[] starts = times.get(times.size() - 1).split(":");

            int endHour = Integer.parseInt(nowDateTime.format(timeFormatter));
            int endMin = Integer.parseInt(nowDateTime.format(timeFormatter2));
            int end = endHour * 60 + endMin;

            int startHour = Integer.parseInt(starts[0]);
            int startMin = Integer.parseInt(starts[1]);
            int start = startHour * 60 + startMin;

            total2 += (end - start);
            resultTime = new Times(total2);
        }
        return resultTime;
    }

    @Data
    static class PostViewDto {

        // Post Info
        private long id;
        private String title;
        private String introduce;
        private String content;
        private String html;
        private LocalDateTime createTime;
        private String formatCreateDate;
        private List<String> times = new ArrayList<>();
        private List<CommentViewDto> comments = new ArrayList<>();
        private int commentCount;
        private StudyState state;
        private Image postImg;
        private Image memberImg;

        // Member Info
        private Long memberId;
        private String memberName;
        private String memberNickname;
        private String memberDesiredJob;


        public PostViewDto(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.introduce = post.getIntroduce();
            this.content = post.getContent();
            this.html = post.getHtml();
            this.createTime = post.getCreateTime();
            this.times = post.getTimes();
            this.comments = post.getComments().stream().map(CommentViewDto::new).collect(Collectors.toList());
            this.commentCount = comments.size();
            this.state = post.getState();
            this.memberName = post.getMember().getName();
            this.memberNickname = post.getMember().getNickname();
            this.memberId = post.getMember().getId();
            this.memberDesiredJob = post.getMember().getDesiredJob();
            this.postImg = post.getImage();
            this.memberImg = post.getMember().getImage();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(LONG).withLocale(Locale.ENGLISH);
            this.formatCreateDate = createTime.format(dateFormatter);
        }

    }

    @Data
    static class CommentViewDto {

        // Comment Info
        private Long id;
        private String content;
        private LocalDateTime createDateTime;
        private int diffMin;
        private Image memberImg;

        // Member Info
        private Long memberId;
        private String memberName;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public CommentViewDto(Comment comment) {

            this.id = comment.getId();
            this.content = comment.getContent();

            this.createDateTime = comment.getCreateTime();
            LocalDateTime nowDateTime = LocalDateTime.now();
            Duration duration = Duration.between(createDateTime, nowDateTime);
            this.diffMin = Math.round(duration.getSeconds()/60);

            this.memberName = comment.getMember().getName();
            this.memberImg = comment.getMember().getImage();
            this.memberId = comment.getMember().getId();
        }
    }

    @Data
    static class MemberDto {

        // member info
        private Long id;
        private String name;
        private Image image;
        private StudyState state;

        public MemberDto(Member member) {
            this.id = member.getId();
            this.name = member.getName();
            this.image = member.getImage();
            this.state = member.getState();
        }
    }

    @Data
    static class Times {

        private int hour;
        private int min;

        public Times(int total) {
            this.hour = Math.round(total / 60);
            this.min = total % 60;
        }

    }



}
