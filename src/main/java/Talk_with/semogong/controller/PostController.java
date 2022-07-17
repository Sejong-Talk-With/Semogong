package Talk_with.semogong.controller;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.dto.PostViewDto;
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
    public String create(@PathVariable("id") Long id, Model model, @SessionAttribute(name = "loginMember", required = false) Long loginMemberId) {
        log.info("posting");
        if (loginMemberId == null) {
            return "redirect:/"; // 오류 처리 필요
        }
        Member loginMember = memberService.findOne(loginMemberId);
        Post post = postService.findOne(id);
        if (loginMember.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        PostEditForm postEditForm = new PostEditForm(post);
        model.addAttribute("postForm", postEditForm);
        model.addAttribute("check", true);
        return "post/editPostForm";
    }

    // 게시글 수정 폼
    @GetMapping("/posts/edit/{id}")
    public String to_edit(@PathVariable("id") Long id, Model model, @SessionAttribute(name = "loginMember", required = false) Long loginMemberId) {
        log.info("posting");
        Member loginMember = memberService.findOne(loginMemberId);
        Post post = postService.findOne(id);
        if (loginMember.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        PostEditForm postEditForm = new PostEditForm(post);
        model.addAttribute("postForm", postEditForm);

        return "post/editPostForm :: #postEdit_container";
    }

    // 게시글 수정
    @PostMapping("/posts/edit/{id}")
    public String edit(@PathVariable("id") Long id, @Valid @ModelAttribute("postForm") PostEditForm postEditForm, Model model, @SessionAttribute(name = "loginMember", required = false) Long loginMemberId) {
        postEditForm.setHtml(markdownToHTML(postEditForm.getContent()));
        postService.edit(postEditForm);
        Member loginMember = memberService.findOne(loginMemberId);

        PostViewDto postViewDto = new PostViewDto(postService.findOne(id));
        model.addAttribute("post", postViewDto);
        model.addAttribute("member", loginMember);
        model.addAttribute("check", true);

        return "components/postModal :: #postModal_content";
    }


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
    public String postDelete(@PathVariable("id") Long id, @SessionAttribute(name = "loginMember", required = false) Long loginMemberId) {
        if (loginMemberId == null) {
            return "redirect:/"; // 오류 처리 필요
        }

        Member loginMember = memberService.findOne(loginMemberId);

        Post post = postService.findOne(id);
        if (loginMember.getId() != post.getMember().getId()) {
            return "redirect:/"; // 오류 처리해줘야 됨.
        }
        if (post.getState() != StudyState.END) {
            memberService.changeState(loginMember.getId(), StudyState.END);
        }
        postService.deletePost(post);
        return "redirect:/";
    }


    // MarkDown To HTML Method
    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }
}
