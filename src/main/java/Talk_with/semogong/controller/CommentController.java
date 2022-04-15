package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Comment;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.service.CommentService;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping("/posts/comment/{id}")
    public String create(@PathVariable("id") Long postId, Authentication authentication, CommentForm commentForm) {
        MyUserDetail userDetail =  (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        Member member = memberService.findByLoginId(loginId);
        Post post = postService.findOne(postId);

        Comment comment = Comment.makeComment(commentForm.getComment(), post, member, LocalDateTime.now());
        commentService.save(comment);

        return "redirect:/";
    }

    @GetMapping("/comment/delete/{id}")
    public String postDelete(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
        return "redirect:/";
    }
}
