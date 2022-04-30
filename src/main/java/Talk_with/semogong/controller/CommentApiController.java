package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Comment;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.service.CommentService;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController// => @Controller + @ResponseBody //RestAPI style로 만들어줌
// @ResponseBody => 데이터 자체를 바로 Json이나 XML로 보낼 때 사용
@RequiredArgsConstructor
public class CommentApiController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/comment/api/new/{id}")
    public Result create(@PathVariable("id") Long postId, Authentication authentication, CommentForm commentForm) {
        MyUserDetail userDetail =  (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        Member member = memberService.findByLoginId(loginId);
        Post post = postService.findOne(postId);

        Comment comment = Comment.makeComment(commentForm.getComment(), post, member, LocalDateTime.now());
        commentService.save(comment);


        return new Result(new CommentViewDto(comment));

    }

    @Data
    @AllArgsConstructor
    static class Result<T>{ // 한번 감싸주기 위함. 그냥 List를 반환하면 JSON으로 list만 반환 되므로 유연성이 떨어짐. 추후에 count등 추가하기 위해선 감싸줘야함
        private T data;
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


}
