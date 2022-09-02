package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequiredArgsConstructor
public class StaticController {

    private final MemberService memberService;
    private final PostService postService;

    @GetMapping("/ranking")
    public String data(Model model) {
        List<MemberDto> members = memberService.findAll().stream().map(MemberDto::new).collect(Collectors.toList());
        Map<MemberDto, Map<Integer, Times>> memberStatic = new HashMap<>();
        List<Integer> days = getDays(LocalDateTime.now());
        for (MemberDto member : members) {
            Map<Integer, Times> staticsData = getStaticsData(member);
            memberStatic.put(member, staticsData);
            member.setTime(staticsData.get(days.get(days.size()-1)));
            if (member.getTime().getHour() != 0) {
                Random rand = new Random();
                int r = rand.nextInt(255);
                int g = rand.nextInt(255);
                int b = rand.nextInt(255);
                member.setColor("rgba(" + String.valueOf(r) + "," + String.valueOf(g) + "," + String.valueOf(b) + "," + "1)");
            } else {
                member.setColor("rgba(236,236,236,1)");
            }
        }

        members.sort(new TimeSorter());
        List<MemberDto> attendSorted = new ArrayList<>(members);
        attendSorted.sort(new AttendSorter());

        model.addAttribute("staticDays", days);
        model.addAttribute("nav", "ranking");
        model.addAttribute("check", true);
        model.addAttribute("staticsDataMap", memberStatic);
        model.addAttribute("members",members.subList(0,5));
        model.addAttribute("attendTop", attendSorted.get(0));
        return "ranking";
    }

    private List<Integer> getDays(LocalDateTime now) {
        List<Integer> days = new ArrayList<>();
        // 0-4 사이 요청 (이틀전까지의 데이터)
        if (now.getHour() < 4) {
            for (int i = 8; i > 1; i--) {
                days.add(LocalDateTime.now().minusDays(i).getDayOfMonth());
            }
        } else { // 이외
            for (int i = 7; i > 0; i--) {
                days.add(LocalDateTime.now().minusDays(i).getDayOfMonth());
            }
        }

        return days;
    }

    private Map<Integer, Times> getStaticsData(MemberDto member) {

        List<Post> posts;
        Map<Integer, Times> dayTimes = new HashMap<>();
        List<Integer> days = getDays(LocalDateTime.now());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 0-4 사이 요청 (이틀전까지의 데이터)
        if (LocalDateTime.now().getHour() < 4) {
            String end = LocalDateTime.now().minusDays(1).format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(8).format(dateTimeFormatter);
            posts = postService.getLast7(member.getId(),start,end);
            for (int i = 8; i > 1; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        } else { // 이외
            String end = LocalDateTime.now().format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(7).format(dateTimeFormatter);
            posts = postService.getLast7(member.getId(),start, end);
            for (int i = 7; i > 0; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        }
        int total = 0;
        for (Post post : posts) {
            Times time = getTimes(post.getTimes());
            total += (time.getHour() * 60 + time.getMin());
            Times totalTimes = new Times(total);
            dayTimes.put(post.getCreateTime().getDayOfMonth(), totalTimes);
        }

        int cnt = 0;
        for (int i = 0; i < days.size(); i++) {
            Times times = dayTimes.get(days.get(i));
            if (times.getHour() == 0 & times.getMin() == 0) {
                cnt += 1;
                if (i != 0) dayTimes.put(days.get(i), dayTimes.get(days.get(i-1)));
            }
        }
        member.setWorkCnt(7-cnt);

        return dayTimes;
    }

    private Times getTimes(List<String> times) {
        Times resultTime;
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
        } else {
            resultTime = new Times(0);
        }
        return resultTime;
    }
}
