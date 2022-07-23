package Talk_with.semogong.domain.att;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class Times {

    private int hour;
    private int min;

    public Times() {

    }

    public Times(int total) {
        this.hour = Math.round(total / 60);
        this.min = total % 60;
    }

}
