package Talk_with.semogong.controller;

import Talk_with.semogong.domain.dto.MemberDto;

import java.util.Comparator;

public class AttendSorter implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        //o1 - o2 = ASC , o2 - o1 = DESC
        MemberDto m1 = (MemberDto) o1;
        MemberDto m2 = (MemberDto) o2;
        return m2.getWorkCnt() - m1.getWorkCnt();
    }
}