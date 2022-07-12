package Talk_with.semogong.domain.dto;

import Talk_with.semogong.controller.HomeController;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.time.format.FormatStyle.LONG;

@Data
public class PostViewDto {

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
