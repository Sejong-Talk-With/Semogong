package Talk_with.semogong.domain.dto;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.att.Times;
import lombok.Data;

import java.awt.*;

@Data
public class MemberDto {

    // member info
    private Long id;
    private String name;
    private Image image;
    private StudyState state;
    private Times time;
    private String color;
    private int workCnt;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.image = member.getImage();
        this.state = member.getState();
    }
}