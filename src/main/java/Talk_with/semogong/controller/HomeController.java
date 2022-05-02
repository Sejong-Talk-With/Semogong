package Talk_with.semogong.controller;

import Talk_with.semogong.domain.*;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @GetMapping("/data")
    public String data(Model model){
        model.addAttribute("nav", "data");
        return "analysis";
    }



    @RequestMapping("/")
    public String home(Model model, Authentication authentication) {

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
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()).get());
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }
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
        model.addAttribute("nav", "home");

        return "home";
    }

    @RequestMapping("/{page}")
    public String home_page(@PathVariable("page") Integer page, Model model, Authentication authentication) {
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
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()).get());
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }
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
        model.addAttribute("nav", "home");

        return "home";
    }

    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "exception", required = false) String exception, Model model, Authentication authentication) {
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
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()).get());
                model.addAttribute("recentPost", memberRecentPostDto);
                if (! ids.contains(memberRecentPostDto.getId())) postModals.add(memberRecentPostDto);
            }
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


}
