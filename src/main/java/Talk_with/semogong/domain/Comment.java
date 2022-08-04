package Talk_with.semogong.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id @GeneratedValue
    @Column(name = "comment_id")
    private long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    private Post post;

    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDateTime createTime;


    // == 연관관계 메서드 ==//
    private void setPost(Post post){
        this.post = post;
        post.getComments().add(this);
    }

    // == 생성 메서드 == //
    public static Comment makeComment(String content, Post post, Member member, LocalDateTime createTime){
        Comment comment = new Comment();
        comment.content = content;
        comment.setPost(post);
        comment.member = member;
        comment.createTime = createTime;
        return comment;
    }

    public void editContent(String content) {
        this.content = content;
    }
}
