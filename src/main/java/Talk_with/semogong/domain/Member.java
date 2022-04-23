package Talk_with.semogong.domain;

import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.domain.form.MemberForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private long id;

    // authorization
    private String loginId;
    private String password;
    private String role;     //권한


    private String name;
    private String nickname;
    private String desiredJob;

    @Enumerated(EnumType.STRING)
    private StudyState state;

    @ElementCollection
    private List<String> links = new ArrayList<>();
    @Embedded
    private Image image;
    private String introduce;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();


    //==생성 메서드==//
    public static Member createMember(String loginId, String password, String name, String nickname, String desiredJob, Image image, String introduce, String... links){
         Member member = new Member();
         member.loginId = loginId;
         member.password = password;
         member.name = name;
         member.nickname = nickname;
         member.desiredJob = desiredJob;
         member.introduce = introduce;
         member.state = StudyState.END;
         return member;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    //==수정 메서드==//
    public void changeState(StudyState state){
        this.state = state;
    }

    public void edit(MemberEditForm memberEditForm) {
        this.name = memberEditForm.getName();
        this.nickname = memberEditForm.getNickname();
        this.desiredJob = memberEditForm.getDesiredJob();
        this.introduce = memberEditForm.getIntroduce();
        this.links = memberEditForm.getLinks();
    }

    //==권한메서드(Setter)==//
    public void setLoginId(String email){
        this.loginId = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
