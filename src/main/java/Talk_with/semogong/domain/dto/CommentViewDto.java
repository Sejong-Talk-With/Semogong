package Talk_with.semogong.domain.dto;

import Talk_with.semogong.domain.Comment;
import Talk_with.semogong.domain.att.Image;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class CommentViewDto {

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
