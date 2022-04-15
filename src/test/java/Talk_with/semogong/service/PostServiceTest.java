package Talk_with.semogong.service;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.StudyState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired private MemberService memberService;
    @Autowired private PostService postService;

    @Test
    public void 게시글저장() throws Exception{
        //given (주어진 것들을 통해)
        Member m1 = Member.createMember(new String("박승일"), "해돌해돌","백엔드 엔지니어", "aws/11234.img","안녕하세요, 17학번 썩은물입니다.", new Image("d","d"),"github.com/bob8dod");
        memberService.save(m1);

        String title = "세모공 데모 테스트";
        String introduce = "당신은 행복합니까?";
        String text = "오늘의 목표\r\n - 세모공 개발 \r\n - 영어 회화 \r\n" ;
        LocalDateTime time = LocalDateTime.now();

        //when (이런 기능을 동작했을 때)
        Post p1 = Post.createPost(m1,title,introduce,text,null,time);
        postService.save(p1);

        //then (이런 결과를 확인할 것)
        Post saved_p = postService.findOne(p1.getId());
        assertEquals(p1,saved_p);

    }

    @Test
    public void 상태클릭() throws Exception{
        //given (주어진 것들을 통해)
        Member m1 = Member.createMember(new String("이해성"), "해돌해돌","백엔드 엔지니어", "aws/11234.img","안녕하세요, 17학번 썩은물입니다.", new Image("d","d"),"github.com/bob8dod");
        memberService.save(m1);

        String title = "세모공 데모 테스트";
        String introduce = "당신은 행복합니까?";
        String text = "오늘의 목표\r\n - 세모공 개발 \r\n - 영어 회화 \r\n" ;
        LocalDateTime time = LocalDateTime.now();

        Post p1 = Post.createPost(m1,title,introduce,text,null,time);
        postService.save(p1);

        //when (이런 기능을 동작했을 때)
        postService.addTime(m1.getId(), LocalDateTime.now());

//        //then (이런 결과를 확인할 것)
//        for (LocalDateTime t : postService.findOne(p1.getId()).getTimes()){
//            System.out.print("t = " + t + "   ");
//        }
//        System.out.println();

    }

}