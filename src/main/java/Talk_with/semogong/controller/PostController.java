package Talk_with.semogong.controller;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.PostEditForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import Talk_with.semogong.service.S3Service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.time.format.FormatStyle.LONG;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final S3Service s3Service;

    // 게시글 생성 폼
    @GetMapping("/posts/new/{id}")
    public String create(@PathVariable("id") Long id, Model model, Authentication authentication) {
        log.info("posting");
        if (authentication == null) {
            return "redirect:/"; // 오류 처리 필요
        }
        Member member = getLoginMemberId(authentication);
        Post post = postService.findOne(id);
        if (member.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        PostEditForm postEditForm = new PostEditForm(post);
        model.addAttribute("postForm", postEditForm);
        model.addAttribute("check", true);
        return "post/editPostForm";
    }

    // 게시글 수정 폼
    @GetMapping("/posts/edit/{id}")
    public String to_edit(@PathVariable("id") Long id, Model model, Authentication authentication) {
        log.info("posting");
        if (authentication == null) {
            return "redirect:/"; // 오류 처리 필요
        }
        Member member = getLoginMemberId(authentication);
        Post post = postService.findOne(id);
        if (member.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        PostEditForm postEditForm = new PostEditForm(post);
        model.addAttribute("postForm", postEditForm);

        return "post/editPostForm :: #postEdit_container";
    }

    // 게시글 수정
    @PostMapping("/posts/edit/{id}")
    public String edit(@PathVariable("id") Long id, @Valid @ModelAttribute("postForm") PostEditForm postEditForm, Model model, Authentication authentication) {
//        if (result.hasErrors()) {
//            log.info("found Null, required re-post");
//            return "post/editPostForm"; }
        postEditForm.setHtml(markdownToHTML(postEditForm.getContent()));
        postService.edit(postEditForm);

        Member member = getLoginMember(authentication);

        PostViewDto postViewDto = new PostViewDto(postService.findOne(id));
        model.addAttribute("post", postViewDto);
        model.addAttribute("member", member);
        model.addAttribute("check", true);

        return "components/postModal :: #postModal_content";
    }

//    @PostMapping("/posts/edit/{id}")
//    public String edit(@PathVariable("id") Long id, @Valid @RequestParam("postForm") PostEditForm postEditForm, BindingResult result) {
//        if (result.hasErrors()) {
//            log.info("found Null, required re-post");
//            return "post/editPostForm :: #postEdit_container";}
//        postEditForm.setHtml(markdownToHTML(postEditForm.getContent()));
//        postService.edit(postEditForm);
//        return "postModal :: #postModal" + id.toString();
//    }

    // 게시글 이미지 업로드 및 변경
    @ResponseBody
    @PostMapping("/posts/edit/{id}/img")
    public Image postImgEdit(@PathVariable("id") Long id, @RequestParam("file") MultipartFile[] files) throws IOException {
        MultipartFile file = files[0];
        Image image = s3Service.upload(file);
        postService.editPostImg(id, image);
        return image;
    }

    // 게시글 삭제
    @DeleteMapping("/posts/delete/{id}")
    public String postDelete(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/"; // 오류 처리 필요
        }
        Member member = getLoginMemberId(authentication);
        Post post = postService.findOne(id);
        if (member.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        if (post.getState() != StudyState.END) {
            memberService.changeState(member.getId(), StudyState.END);
        }
        postService.deletePost(post);
        return "redirect:/";
    }

    // Login 된 회원의 Member Entity 조회 Method
    private Member getLoginMemberId(Authentication authentication) {
        MyUserDetail userDetail =  (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 객체)
        String loginId = userDetail.getEmail();
        return memberService.findByLoginId(loginId);
    }

    // MarkDown To HTML Method
    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
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
        private List<HomeController.CommentViewDto> comments = new ArrayList<>();
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
            this.comments = post.getComments().stream().map(HomeController.CommentViewDto::new).collect(Collectors.toList());
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

    private Member getLoginMember(Authentication authentication) {
        MyUserDetail userDetail = (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        return memberService.findByLoginId(loginId); // "박승일"로 로그인 했다고 가정, 해당 로그인된 회원의 ID를 가져옴
    }
}
