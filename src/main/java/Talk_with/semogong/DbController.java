/*
package Talk_with.semogong;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.repository.PostNativeRepository;
import Talk_with.semogong.repository.PostRepository;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DbController {

    private final InitService initService;


    @PostConstruct
    public void dbInit() {
//        initService.dbInit1();
//        initService.dbInit2();
//        initService.dbInit3();
        initService.dbInit4();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final MemberService memberService;
        private final PostService postService;
        private final PostRepository postRepository;

        public void dbInit1() {
            List<Member> all = memberService.findAll();
            for (Member member : all) {
                List<Post> posts = postService.getLast7(member.getId(), "2022-04-17", "2022-06-01");
                for (Post post : posts) {
                    Post one = postService.findOne(post.getId());
                    List<String> times = one.getTimes();
                    List<String> newTimes = new ArrayList<>();
                    for (String time : times) {
                        if (newTimes.contains(time)) {
                            break;
                        }
                        newTimes.add(time);
                    }
                    one.setTimes(newTimes);
                }
            }
        }

        public void dbInit2() {
            Post post = postService.findOne(755L);
            List<String> times = new ArrayList<>();
            times.add("01:00");
            times.add("03:59");
            post.setTimes(times);

            Post post2 = postService.findOne(763L);
            List<String> times2 = new ArrayList<>();
            times2.add("04:00");
            times2.add("06:00");
            times2.add("12:08");
            post2.setTimes(times2);
        }

        public void dbInit3() {
            Post post = postService.findOne(755L);
            post.setCreateTime(LocalDateTime.of(2022,8,13,23,59));
        }

        @Transactional
        public void dbInit4() {
            Member lee = memberService.findByName("이재훈");
            Post post = Post.createPost(lee, LocalDateTime.of(2022, 9, 3, 10, 30, 23));
            post.setTimes(List.of(new String[]{"10:30","13:00","15:00", "21:00"}));
            post.editState(StudyState.END);
            postRepository.save(post);

            Post post2 = Post.createPost(lee, LocalDateTime.of(2022, 9, 4, 10, 30, 23));
            post2.setTimes(List.of(new String[]{"10:30","13:00","15:00", "21:00"}));
            post2.editState(StudyState.END);
            postRepository.save(post2);

            lee.changeState(StudyState.END);
        }
    }
}
*/
