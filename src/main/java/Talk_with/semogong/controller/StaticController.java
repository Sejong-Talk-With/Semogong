package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.domain.dto.PostViewDto;
import Talk_with.semogong.repository.PostNativeRepository;
import Talk_with.semogong.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequiredArgsConstructor
public class StaticController {

    private final MemberService memberService;
    private final PostNativeRepository postNativeRepository;

    @GetMapping("/data")
    public String data(Model model, Authentication authentication) throws IOException {
        if (authentication == null) {
            return "redirect:/";
        }
        List<MemberDto> members = memberService.findAll().stream().map(MemberDto::new).collect(Collectors.toList());
        Map<MemberDto, Map<Integer, Times>> memberStatic = new HashMap<>();
        for (MemberDto member : members) {
            memberStatic.put(member, getStaticsData(member));
        }
        model.addAttribute("staticDays", memberStatic.get(members.get(0)).keySet().toArray());
        model.addAttribute("nav", "data");
        model.addAttribute("check", true);
        model.addAttribute("staticsDataMap", memberStatic);
        return "analysis";
    }

    private Map<Integer, Times> getStaticsData(MemberDto member) {

        List<Post> posts;
        Map<Integer, Times> dayTimes = new HashMap<>();

        // 0-4 사이 요청 (이틀전까지의 데이터)
        if (0 < LocalDateTime.now().getHour() & LocalDateTime.now().getHour() < 4) {
            posts = postNativeRepository.getLast7(member.getId(), 2);
            for (int i = 8; i > 1; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        } else { // 이외
            posts = postNativeRepository.getLast7(member.getId(),1);
            for (int i = 7; i > 0; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        }

        for (Post post : posts) {
            dayTimes.put(post.getCreateTime().getDayOfMonth(), getTimes(post.getTimes()));
        }
        return dayTimes;
    }

    private Times getTimes(List<String> times) {
        Times resultTime = null;
        if (times.size() % 2 == 0) {
            int total1 = 0;
            for (int i = 1; i < times.size(); i += 2) {
                String[] ends = times.get(i).split(":");
                String[] starts = times.get(i - 1).split(":");
                int endHour = Integer.parseInt(ends[0]);
                if (0 <= endHour & endHour < 4) {
                    endHour += 24;
                }
                int endMin = Integer.parseInt(ends[1]);
                int end = endHour * 60 + endMin;


                int startHour = Integer.parseInt(starts[0]);
                if (0 <= startHour & startHour < 4) {
                    startHour += 24;
                }
                int startMin = Integer.parseInt(starts[1]);
                int start = startHour * 60 + startMin;

                total1 += (end - start);
            }
            resultTime = new Times(total1);
        }
        return resultTime;
    }
}
