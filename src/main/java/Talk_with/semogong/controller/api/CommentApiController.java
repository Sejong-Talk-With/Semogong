package Talk_with.semogong.controller.api;

import Talk_with.semogong.configuration.SessionConst;
import Talk_with.semogong.controller.HomeController;
import Talk_with.semogong.domain.Comment;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.CommentForm;
import Talk_with.semogong.service.CommentService;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.FormatStyle.LONG;

// @RestController// => @Controller + @ResponseBody //RestAPI style로 만들어줌
// @ResponseBody => 데이터 자체를 바로 Json이나 XML로 보낼 때 사용
@Controller
@RequiredArgsConstructor
public class CommentApiController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/comment/api/new/{id}")
    public String create(@PathVariable("id") Long postId, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId, Model model, @RequestParam Map<String, Object> paramMap) {
        Member member = memberService.findOne(loginMemberId);
        Post post = postService.findOne(postId);
        Comment comment = Comment.makeComment(paramMap.get("comment").toString(), post, member, LocalDateTime.now());
        commentService.save(comment);

        PostViewDto postViewDto = new PostViewDto(post);
        MemberDto memberDto = new MemberDto(member);

        model.addAttribute("post", postViewDto);
        model.addAttribute("check", true);
        model.addAttribute("member", memberDto);
        return "components/commentList :: #commentPart";

    }

    // 댓글 삭제
    @DeleteMapping("/comment/delete/{id}")
    public String commentDelete(@PathVariable("id") Long id, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId, Model model, @RequestParam Long postId) {
        commentService.deleteComment(id);
        Member member = memberService.findOne(loginMemberId);
        Post post = postService.findOne(postId);

        PostViewDto postViewDto = new PostViewDto(post);
        MemberDto memberDto = new MemberDto(member);

        model.addAttribute("post", postViewDto);
        model.addAttribute("check", true);
        model.addAttribute("member", memberDto);

        return "components/commentList :: #commentPart";
    }


    @Data
    @AllArgsConstructor
    static class Result<T>{ // 한번 감싸주기 위함. 그냥 List를 반환하면 JSON으로 list만 반환 되므로 유연성이 떨어짐. 추후에 count등 추가하기 위해선 감싸줘야함
        private T data;
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
