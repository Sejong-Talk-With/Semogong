package Talk_with.semogong.controller;

import Talk_with.semogong.domain.*;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final MemberService memberService;


    @RequestMapping("/")
    public String home(Model model, Authentication authentication) {

        log.info("opened home");
        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);

            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()));
                model.addAttribute("recentPost", memberRecentPostDto);
            }
        } else {
            model.addAttribute("check", false);
        }
        List<Post> posts = postService.findByPage(0);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());

        model.addAttribute("page", -1);
        model.addAttribute("posts", postDtos);
        model.addAttribute("commentForm", new CommentForm());
        return "home";
    }

    @RequestMapping("/{page}")
    public String home_page(@PathVariable("page") Integer page, Model model, Authentication authentication) {
        log.info("opened home");
        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);

            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()));
                model.addAttribute("recentPost", memberRecentPostDto);
            }
        } else {
            model.addAttribute("check", false);
        }
        List<Post> posts = postService.findByPage((page - 1) * 12);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());
        model.addAttribute("posts", postDtos);
        model.addAttribute("page", page);
        model.addAttribute("commentForm", new CommentForm());
        return "home";
    }

    @GetMapping("/auth/login")
    public String login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "exception", required = false) String exception, Model model, Authentication authentication) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        if (authentication != null) {
            Member member = getLoginMember(authentication);
            MemberForm memberForm = createMemberForm(member);

            model.addAttribute("check", true);
            model.addAttribute("member", memberForm);
            if (member.getState() == StudyState.STUDYING || member.getState() == StudyState.BREAKING) {
                PostViewDto memberRecentPostDto = new PostViewDto(postService.getRecentPost(member.getId()));
                model.addAttribute("recentPost", memberRecentPostDto);
            }
        } else {
            model.addAttribute("check", false);
        }
        List<Post> posts = postService.findByPage(0);
        List<PostViewDto> postDtos = posts.stream().map(PostViewDto::new).collect(Collectors.toList());

        model.addAttribute("page", -1);
        model.addAttribute("posts", postDtos);
        model.addAttribute("commentForm", new CommentForm());
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
        private List<String> times = new ArrayList<>();
        private List<CommentViewDto> comments = new ArrayList<>();
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
            this.state = post.getState();
            this.memberName = post.getMember().getName();
            this.memberNickname = post.getMember().getNickname();
            this.memberId = post.getMember().getId();
            this.memberDesiredJob = post.getMember().getDesiredJob();
            this.postImg = post.getImage();
            this.memberImg = post.getMember().getImage();
        }

    }

    @Data
    static class CommentViewDto {

        // Comment Info
        private Long id;
        private String content;
        private LocalDateTime createTime;
        private Image memberImg;

        // Member Info
        private Long memberId;
        private String memberName;

        public CommentViewDto(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.createTime = comment.getCreateTime();
            this.memberName = comment.getMember().getName();
            this.memberImg = comment.getMember().getImage();
            this.memberId = comment.getMember().getId();
        }
    }


}
