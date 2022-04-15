package Talk_with.semogong.service;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired public MemberService memberService;
    @Autowired public MemberRepository memberRepository;


    @Test
    public void 회원저장() throws Exception{
        //given (주어진 것들을 통해)
        Member m1 = Member.createMember(new String("박승일"), "해돌해돌","백엔드 엔지니어", "aws/11234.img","안녕하세요, 17학번 썩은물입니다.",new Image("d","d"),"github.com/bob8dod");

        //when (이런 기능을 동작했을 때)
        memberService.save(m1);
        Member saved_m = memberService.findOne(m1.getId());

        //then (이런 결과를 확인할 것)
        assertEquals(saved_m,m1);

    }
    @Test
    public void 회원상태조회() throws Exception{
        //given (주어진 것들을 통해)
        Member m1 = Member.createMember(new String("박승일"), "해돌해돌","백엔드 엔지니어", "aws/11234.img","안녕하세요, 17학번 썩은물입니다.",new Image("d","d"),"github.com/bob8dod");
        memberService.save(m1);

        //when1 (이런 기능을 동작했을 때)
        StudyState state = memberService.checkState(m1.getId());

        //then1 (이런 결과를 확인할 것)
        assertEquals(StudyState.END, state);
    }

    @Test
    public void 회원상태수정() throws Exception{
        //given (주어진 것들을 통해)
        Member m1 = Member.createMember(new String("박승일"), "해돌해돌","백엔드 엔지니어", "aws/11234.img","안녕하세요, 17학번 썩은물입니다.",new Image("d","d"),"github.com/bob8dod");
        memberService.save(m1);

        //when (이런 기능을 동작했을 때)
        memberService.changeState(m1.getId(), StudyState.STUDYING);

        //then (이런 결과를 확인할 것)
        assertEquals(StudyState.STUDYING, memberService.checkState(m1.getId()));

    }
}