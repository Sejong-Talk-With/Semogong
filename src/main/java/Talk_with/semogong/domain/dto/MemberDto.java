package Talk_with.semogong.domain.dto;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.att.Goal;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.att.Times;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemberDto {

    // member info
    private long id;

    private String loginId;

    private String name;
    private String nickname;
    private String desiredJob;

    private StudyState state;

    private List<String> links = new ArrayList<>();
    private Image image;
    private String introduce;

    private Times time;
    private Times totalTime;
    private int workCnt;
    private Goal goal;
    private String color;

    public MemberDto(Member member) {
        this.setId(member.getId());
        this.setLoginId(member.getLoginId());
        this.setName(member.getName());
        this.setNickname(member.getNickname());
        this.setDesiredJob(member.getDesiredJob());
        this.setIntroduce(member.getIntroduce());
        this.setLinks(member.getLinks());
        this.setImage(member.getImage());
        this.setState(member.getState());
        this.setGoal(member.getGoal());
    }
}