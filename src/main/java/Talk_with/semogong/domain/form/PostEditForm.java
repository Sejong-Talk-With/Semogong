package Talk_with.semogong.domain.form;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PostEditForm {

    // Post Info
    private Long id;
    @NotEmpty(message = "제목은 필수로 입력하셔야 됩니다.")
    private String title;
    private String introduce;
    private String content;
    private String html;
    private StudyState state;
    private List<String> times;
    private Image image;

    // Member Info
//    private String memberName;
//    private String memberNickname;
//    private String memberDesiredJob;

    public PostEditForm() { } // 빈 생성자를 설정해 두지 않으면, 오류 발생! 아마 MVC에서 해당 객체를 받아오는 과정에서 생성자를 사용해서 받아오는 듯!
                                // 근데 만약 이 빈 생성자를 설정하지 않고, 값을 받는 생성자를 사용하면, 그 주고 받는 당시에 생성하는데 필요한 인자들이 없기에 오류 발생

    public PostEditForm(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.introduce = post.getIntroduce();
        this.content = post.getContent();
        this.html = post.getHtml();
        this.state = post.getState();
        this.times = post.getTimes();
//        this.memberName = member.getName();
//        this.memberNickname = member.getNickname();;
//        this.memberDesiredJob = member.getDesiredJob();
        this.image = post.getImage();
    }

}
