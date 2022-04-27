package Talk_with.semogong.domain.att;

import java.util.Arrays;
import java.util.List;

public class DesiredJob {
    static final List<String> jobList =  Arrays.asList("백엔드 엔지니어", "데이터 엔지니어", "ML 엔지니어", "프론트엔드 엔지니어");
    public static List<String> getJobList() {
        return jobList;
    }
}
