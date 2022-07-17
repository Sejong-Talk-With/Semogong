package Talk_with.semogong.controller;

import Talk_with.semogong.domain.dto.MemberDto;

import java.util.Comparator;

public class TimeSorter implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        //o1 - o2 = ASC , o2 - o1 = DESC
        MemberDto m1 = (MemberDto) o1;
        MemberDto m2 = (MemberDto) o2;
        int totalTime1 = 0;
        int totalTime2 = 0;
        if (m1.getTime() != null) totalTime1 = m1.getTime().getHour() * 60 + m1.getTime().getMin();
        if (m2.getTime() != null) totalTime2 = m2.getTime().getHour() * 60 + m2.getTime().getMin();
        return totalTime2 - totalTime1;
    }
}