package Talk_with.semogong.domain.att;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Times {

    private int hour;
    private int min;

    public Times(int total) {
        this.hour = Math.round(total / 60);
        this.min = total % 60;
    }

}