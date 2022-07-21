package Talk_with.semogong.domain.att;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Embeddable
@Getter
public class Goal {

    private int dayGoalTimes;
    private int weekGoalTimes;

    public Goal(int day, int week) {
        this.dayGoalTimes = day;
        this.weekGoalTimes = week;
    }

    public Goal() { }
}
