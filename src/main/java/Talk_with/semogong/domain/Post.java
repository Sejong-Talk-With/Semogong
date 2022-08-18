package Talk_with.semogong.domain;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.form.PostEditForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id @GeneratedValue
    @Column(name = "post_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    private String title;
    private String introduce;
    @Enumerated(EnumType.STRING)
    private StudyState state;
    @Column(columnDefinition="TEXT")
    private String content;
    @Column(columnDefinition="TEXT")
    private String html;
    @ElementCollection
    private List<String> times = new ArrayList<>();
    @Embedded
    private Image image;


    @OneToMany(mappedBy = "post" , cascade={CascadeType.REMOVE})
    private List<Comment> comments = new ArrayList<>();

    private LocalDateTime createTime;

    //==연관관계 메서드==//
    private void setMember(Member member){
        this.member = member;
        member.getPosts().add(this);
    }

    //==생성 메서드==//
    public static Post createPost(Member member, LocalDateTime createTime){
        Post post = new Post();
        post.setMember(member);
        post.createTime = createTime;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        post.times.add(LocalDateTime.now().format(timeFormatter));
        post.state = StudyState.STUDYING;
        return post;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setTimes(List<String> times) {
        this.times = new ArrayList<>(times);
    }

    //==수정 메서드==//
    public void edit(PostEditForm postEditForm) {
        this.title = postEditForm.getTitle();
        this.introduce = postEditForm.getIntroduce();
        this.content = postEditForm.getContent();
        this.html = postEditForm.getHtml();
        this.times = postEditForm.getTimes();
    }

    public void addTime(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeString = time.format(formatter);
        this.times.add(timeString);
    }

    public void editState(StudyState state) {
        this.state = state;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }


}
