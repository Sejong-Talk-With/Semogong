package Talk_with.semogong.domain.form;

import Talk_with.semogong.domain.Image;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter @Setter
public class PostForm {

    @NotEmpty(message = "제목은 필수 입력 값입니다.")
    private String title;
    private Image image;
    private String introduce;
    private String content;
    private String html;
    private LocalDateTime time;

}
