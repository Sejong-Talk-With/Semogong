package Talk_with.semogong.domain.form;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MemberForm {

    private Long id;
    @NotEmpty(message = "ID는 필수로 입력하셔야 됩니다.")
    private String loginId;
    @NotEmpty(message = "비밀번호는 필수로 입력하셔야 됩니다.")
    private String password;

    @NotEmpty(message = "이름은 필수로 입력하셔야 됩니다.")
    private String name;
    private String nickname;
    private String desiredJob;
    private StudyState state;

    private List<String> links = new ArrayList<>();
    private Image image;

    private String introduce;

}
