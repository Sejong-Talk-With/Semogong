package Talk_with.semogong.domain.form;

import Talk_with.semogong.domain.att.Image;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemberEditForm{

    private Long id;
    @NotEmpty(message = "이름은 필수로 입력하셔야 됩니다.")
    private String name;
    private String nickname;
    private String desiredJob;
    private String introduce;

    private List<String> links = new ArrayList<>();
    private Image image;
}
