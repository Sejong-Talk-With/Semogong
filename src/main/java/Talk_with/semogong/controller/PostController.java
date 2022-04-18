package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.PostEditForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import Talk_with.semogong.service.S3Service;
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

@Controller
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final S3Service s3Service;

    @GetMapping("/posts/edit/{id}")
    public String to_edit(@PathVariable("id") Long id, Model model, Authentication authentication) {
        log.info("posting");
        Member member = getLoginMemberId(authentication);
        Post post = postService.findOne(id);
        if (member.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        PostEditForm postEditForm = new PostEditForm(post, member);
        model.addAttribute("postForm", postEditForm);
        return "post/editPostForm";
    }

    @PostMapping("/posts/edit/{id}")
    public String edit(@PathVariable("id") Long id, @Valid @ModelAttribute("postForm") PostEditForm postEditForm, BindingResult result) {
        if (result.hasErrors()) {
            log.info("found Null, required re-post");
            return "post/editPostForm"; }
        postEditForm.setHtml(markdownToHTML(postEditForm.getContent()));
        postService.edit(postEditForm);
        return "redirect:/";
    }


    @PostMapping("/posts/edit/{id}/img")
    public void postImgEdit(@PathVariable("id") Long id, @RequestParam("file") MultipartFile[] files) throws IOException {
        MultipartFile file = files[0];
        Image image = s3Service.upload(file);
        postService.editPostImg(id, image);
    }

    @GetMapping("/posts/delete/{id}")
    public String postDelete(@PathVariable("id") Long id, Authentication authentication) {
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

    private Member getLoginMemberId(Authentication authentication) {
        MyUserDetail userDetail =  (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        return memberService.findByLoginId(loginId); // "박승일"로 로그인 했다고 가정, 해당 로그인된 회원의 ID를 가져옴
    }

    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }
}
