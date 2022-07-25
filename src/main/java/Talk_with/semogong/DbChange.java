/*
package Talk_with.semogong;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.repository.PostNativeRepository;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DbChange {

    private final InitService initService;


    @PostConstruct
    public void dbInit() {
        initService.dbInit1();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final MemberService memberService;
        private final PostNativeRepository postNativeRepository;
        private final PostService postService;

        public void dbInit1() {
            // 5/1 - 5/7
            List<Member> all = memberService.findAll();
            for (Member member : all) {
                List<Post> posts = postNativeRepository.getBetween(member.getId(), "2022-04-17", "2022-06-01");
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
    }
}
*/
