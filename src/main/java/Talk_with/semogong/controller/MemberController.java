package Talk_with.semogong.controller;

import Talk_with.semogong.configuration.SessionConst;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.DesiredJob;
import Talk_with.semogong.domain.att.Goal;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.domain.dto.PostViewDto;
import Talk_with.semogong.domain.form.LoginForm;
import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import Talk_with.semogong.service.S3Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {


    private final MemberService memberService;
    private final S3Service s3Service;
    private final PostService postService;

    // 회원가입 폼
    @GetMapping("/signup")
    public String signUpForm(Model model) {
        List<String> jobList = DesiredJob.getJobList();
        log.info("signup");
        model.addAttribute("memberForm", new MemberForm());
        model.addAttribute("jobs", jobList);
        return "member/createMemberForm";
    }

    // 회원가입 진행
    @PostMapping("/signup")
    public String signUp(@Valid MemberForm memberForm, BindingResult result) {
        if (result.hasErrors()) {
            log.info("found Null, re-joining");
            return "member/createMemberForm";
        }
        Member member = Member.createMember(memberForm.getLoginId(), memberForm.getPassword(), memberForm.getName(), memberForm.getNickname(), memberForm.getDesiredJob(), null, memberForm.getIntroduce(), null, null);
        member.setRole("USER");
        member.setGoal(new Goal(300, 1500));
        memberService.save(member);
        return "redirect:/";
    }

    // 회원 정보 수정 폼
    @GetMapping("/edit/{id}")
    public String memberEditForm(@PathVariable("id") Long id, Model model) {
        List<String> jobList = DesiredJob.getJobList();
        Member member = memberService.findOne(id);
        MemberEditForm memberEditForm = createMemberEditFrom(member);
        model.addAttribute("memberEditForm", memberEditForm);
        model.addAttribute("jobs", jobList);
        return "member/editMemberForm";
    }

    // 회원 정보 수정 (이미지 업로드 및 수정 폼 포함)
    @PostMapping("/edit/{id}")
    public String memberEdit(@PathVariable("id") Long id, @Valid MemberEditForm memberEditForm, BindingResult result,
                             @RequestParam(defaultValue = "/") String redirectURL) {
        List<String> links = memberEditForm.getLinks();
        while (links.remove("")) {
        }
        memberEditForm.setLinks(links);
        if (result.hasErrors()) {
            log.info("found Null, re-editing");
            return "member/editMemberForm";
        }
        memberService.editMember(id, memberEditForm);
        return "redirect:" + redirectURL;
    }

    // 회원 이미지 업로드 및 수정
    @PostMapping("/edit/{id}/img")
    public void memberImgEdit(@PathVariable("id") Long id, @RequestParam("file") MultipartFile[] files) throws IOException {
        MultipartFile file = files[0];
        Image image = s3Service.upload(file);
        memberService.editMemberImg(id, image);
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult,
                        @RequestParam(name = "redirectURL", defaultValue = "/") String redirectURL,
                        @RequestParam(name = "focus", required = false) String focus,
                        HttpServletRequest request, RedirectAttributes redirectAttributes) {


        // form 형식에 맞지 않은 제출
        if (bindingResult.hasErrors()) {
            return "login";
        }

        // 로그인 검증         // Global Error -> Object 단 오류! (직접 처리해야 되는 부분)
        Optional<Member> loginMember = memberService.findByLoginId(loginForm.getLoginId());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (loginMember.isEmpty() || !passwordEncoder.matches(loginForm.getPassword(), loginMember.get().getPassword())) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login";
        }

        // 로그인 성공
        HttpSession session = request.getSession();
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember.get().getId());
        if (focus != null) redirectAttributes.addAttribute("focus", focus);
        return "redirect:" + redirectURL;
    }

    // 회원 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; //주소 요청으로 변경
    }

    @GetMapping("/my-page")
    public String myPage(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId,
                         Model model, @RequestParam(name = "month", required = false) Integer month) {
        Member oriMember = memberService.findOne(loginMemberId);
        MemberDto member = new MemberDto(oriMember);
        List<Integer> days = getDays(LocalDateTime.now());
        Map<Integer, Times> staticsData = getStaticsData(member);
        member.setTotalTime(getAllTimes(oriMember));
        int year = LocalDateTime.now().getYear();
        LocalDateTime focusedDate; // 캘린더용 변수
        if (month == null) {
            month = LocalDateTime.now().getMonthValue();
        }
        focusedDate = LocalDateTime.of(year, month, 1, 0, 0);
        List<Post> monthPosts = postService.getMonthPosts(member.getId(), month);
        List<PostViewDto> monthPostDtos = monthPosts.stream().map(PostViewDto::new).collect(Collectors.toList());
        int[] dayData = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        List<String> weekDay = Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");
        List<Integer> calenderDays = new ArrayList<>();
        Map<Integer, PostViewDto> calenderData = new HashMap<>();
        for (int i = 1; i <= dayData[month - 1]; i++) {
            calenderDays.add(i);
            calenderData.put(i, null);
        }
        for (PostViewDto monthPostDto : monthPostDtos) {
            int dayOfMonth = monthPostDto.getCreateTime().getDayOfMonth();
            calenderData.put(dayOfMonth, monthPostDto);
        }

        List<Post> nowMonthPosts = postService.getMonthPosts(loginMemberId, LocalDateTime.now().getMonthValue());
        int focusedDay;
        if (LocalDateTime.now().getHour() < 4) {
            focusedDay = LocalDateTime.now().getDayOfMonth() - 2;
        } else {
            focusedDay = LocalDateTime.now().getDayOfMonth() - 1;
        }
        if (focusedDay <= 0) focusedDay = 0;
        List<Post> calculatedNowMonthPosts = new ArrayList<>();
        for (Post post : nowMonthPosts) {
            if (post.getCreateTime().getDayOfMonth() > focusedDay) {
                break;
            }
            calculatedNowMonthPosts.add(post);
        }


        ForCalender CalenderInfo = getCalenderInfo(weekDay, focusedDate);
        int monthDate = dayData[month - 1];
        if (month == LocalDateTime.now().getMonthValue()) {
            monthDate = focusedDay;
            monthPosts = new ArrayList<>(calculatedNowMonthPosts);
        }


        AllStatic allStatic = getAllStatus(oriMember, staticsData, days.get(days.size() - 1), monthPosts, monthDate, focusedDay, calculatedNowMonthPosts.size());



        model.addAttribute("nav", "myPage");
        model.addAttribute("member", member);
        model.addAttribute("staticDays", days);
        model.addAttribute("staticData", staticsData);
        model.addAttribute("month", month);
        model.addAttribute("calenderInfo", CalenderInfo);
        model.addAttribute("calenderDays", calenderDays);
        model.addAttribute("calenderData", calenderData);
        model.addAttribute("postModals", monthPostDtos);
        model.addAttribute("goalDto", new GoalDto());
        model.addAttribute("allStatic", allStatic);


        return "member/myPage";
    }

    @GetMapping("/profile/{id}")
    public String memberProfile(@PathVariable(name = "id") Long memberId, Model model,
                                @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Long loginMemberId,
                                @RequestParam(name = "month", required = false) Integer month) {
        Member oriMember = memberService.findOne(memberId);
        MemberDto member = new MemberDto(oriMember);
        List<Integer> days = getDays(LocalDateTime.now());
        Map<Integer, Times> staticsData = getStaticsData(member);
        member.setTotalTime(getAllTimes(oriMember));
        int year = LocalDateTime.now().getYear();
        LocalDateTime focusedDate;
        if (month == null) {
            month = LocalDateTime.now().getMonthValue();
            focusedDate = LocalDateTime.of(year, month, 1, 0, 0);
        } else {
            focusedDate = LocalDateTime.of(year, month, 1, 0, 0);
        }
        List<Post> monthPosts = postService.getMonthPosts(memberId, month);
        List<PostViewDto> monthPostDtos = monthPosts.stream().map(PostViewDto::new).collect(Collectors.toList());
        int[] dayData = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        List<String> weekDay = Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");
        List<Integer> calenderDays = new ArrayList<>();
        Map<Integer, PostViewDto> calenderData = new HashMap<>();
        for (int i = 1; i <= dayData[month - 1]; i++) {
            calenderDays.add(i);
            calenderData.put(i, null);
        }
        for (PostViewDto monthPostDto : monthPostDtos) {
            int dayOfMonth = monthPostDto.getCreateTime().getDayOfMonth();
            calenderData.put(dayOfMonth, monthPostDto);
        }

        List<Post> nowMonthPosts = postService.getMonthPosts(memberId, LocalDateTime.now().getMonthValue());
        int focusedDay;
        if (LocalDateTime.now().getHour() < 4) {
            focusedDay = LocalDateTime.now().getDayOfMonth() - 2;
        } else {
            focusedDay = LocalDateTime.now().getDayOfMonth() - 1;
        }
        List<Post> calculatedNowMonthPosts = new ArrayList<>();
        for (Post post : nowMonthPosts) {
            if (post.getCreateTime().getDayOfMonth() > focusedDay) {
                break;
            }
            calculatedNowMonthPosts.add(post);
        }

        ForCalender CalenderInfo = getCalenderInfo(weekDay, focusedDate);
        int monthDate = dayData[month - 1];
        if (month == LocalDateTime.now().getMonthValue()) {
            monthDate = focusedDay;
            monthPosts = new ArrayList<>(calculatedNowMonthPosts);
        }


        AllStatic allStatic = getAllStatus(oriMember, staticsData, days.get(days.size() - 1), monthPosts, monthDate, focusedDay, calculatedNowMonthPosts.size());


        model.addAttribute("loginId", loginMemberId);
        model.addAttribute("member", member);
        model.addAttribute("staticDays", days);
        model.addAttribute("staticData", staticsData);
        model.addAttribute("month", month);
        model.addAttribute("calenderInfo", CalenderInfo);
        model.addAttribute("calenderDays", calenderDays);
        model.addAttribute("calenderData", calenderData);
        model.addAttribute("postModals", monthPostDtos);
        model.addAttribute("goalDto", new GoalDto());
        model.addAttribute("allStatic", allStatic);


        return "member/memberDetail";
    }

    private AllStatic getAllStatus(Member member, Map<Integer, Times> staticData, Integer date, List<Post> monthPosts, int monthDate, int focusedDay, int nowMonthPostsLen) {
        AllStatic allStatic = new AllStatic();

        allStatic.setAllTimes(getAllTimes(member));

        int weekAllTimes = 0;
        for (Times times : staticData.values()) {
            weekAllTimes = weekAllTimes + (times.getHour() * 60 + times.getMin());
        }
        allStatic.setWeekAllTimes(new Times(weekAllTimes));

        Times weekAvgTimes = new Times();
        int weekAvgTime = weekAllTimes / 7;
        weekAvgTimes.setHour(Math.floorDiv(weekAvgTime, 60));
        weekAvgTimes.setMin(weekAvgTime % 60);
        allStatic.setWeekAvgTimes(weekAvgTimes);

        int monthAllTimes = 0;
        for (Post post : monthPosts) {
            if (post.getTimes().size() % 2 != 0) continue;
            Times times = getTimes(post.getTimes());
            monthAllTimes = monthAllTimes + (times.getHour() * 60 + times.getMin());
        }
        allStatic.setMonthAllTimes(new Times(monthAllTimes));

        Times monthAvgTimes = new Times();
        int monthAvgTime = 0; // (만약 현재 보고 있는 달이 현재 월과 동일할 때) 매달 1일 ~ 2일 새벽 4시 전
        if(monthDate > 0) monthAvgTime = monthAllTimes / monthDate;  // 평상 시
        monthAvgTimes.setHour(Math.floorDiv(monthAvgTime, 60));
        monthAvgTimes.setMin(monthAvgTime % 60);
        allStatic.setMonthAvgTimes(monthAvgTimes);

        allStatic.setMonthAttendanceCnt(monthPosts.size());
        allStatic.setMonthDate(monthDate);
        if (monthDate <= 0) { // (만약 현재 보고 있는 달이 현재 월과 동일할 때) 매달 1일 ~ 2일 새벽 4시 전
            allStatic.setMonthAttendanceRate(0);
        } else {
            allStatic.setMonthAttendanceRate(Math.round(((float) monthPosts.size() / (float) monthDate) * 100));
        }
        Times yesterdayStudyTimes = staticData.get(date);
//        int yesterdayStudyTime = yesterdayStudyTimes.getHour() * 60 + yesterdayStudyTimes.getMin();
        int dayGoalTimes = member.getGoal().getDayGoalTimes();
        allStatic.setGoalAttainmentToday(Math.round(((float) weekAvgTime / (float) dayGoalTimes) * 100));

        int weekGoalTimes = member.getGoal().getWeekGoalTimes();
        allStatic.setGoalAttainmentWeek(Math.round(((float) weekAllTimes / (float) weekGoalTimes) * 100));

        if (focusedDay <= 0) { // 매달 1일 ~ 2일 새벽 4시 전
            allStatic.setNowMonthAttRate(0);
            allStatic.setStudyRankRate(Math.floorDiv(allStatic.getGoalAttainmentToday() + allStatic.getGoalAttainmentWeek(), 2));
        } else { // 평상 시
            allStatic.setNowMonthAttRate(Math.round(((float) nowMonthPostsLen / (float) focusedDay) * 100));
            allStatic.setStudyRankRate(Math.floorDiv(allStatic.getNowMonthAttRate() + allStatic.getGoalAttainmentToday() + allStatic.getGoalAttainmentWeek(), 3));
        }

        return allStatic;
    }

    @GetMapping("/edit/{id}/goal")
    public String editGoal(@PathVariable(name = "id") Long id, Model model) {
        Member member = memberService.findOne(id);
        Goal goal = member.getGoal();
        GoalDto goalDto = new GoalDto(goal);
        model.addAttribute("id", id);
        model.addAttribute("goalDto", goalDto);
        return "components/editGoal :: #editForm";
    }

    @PostMapping("/edit/{id}/goal")
    public String editGoalRedirect(@PathVariable(name = "id") Long id, @ModelAttribute(name = "goalDto") GoalDto goalDto) {
        Goal goal = new Goal(goalDto.getDayHour() * 60 + goalDto.getDayMin(), goalDto.getWeekHour() * 60 + goalDto.getWeekMin());
        memberService.editMemberGoal(id, goal);
        return "redirect:/members/my-page";
    }

    private ForCalender getCalenderInfo(List<String> emptyDate, LocalDateTime date) {

        String month = date.getMonth().toString();
        int year = date.getYear();
        int start = emptyDate.indexOf(date.getDayOfWeek().toString().toLowerCase(Locale.ENGLISH));
        ForCalender CalenderInfo = new ForCalender(year, month, start);
        return CalenderInfo;
    }

    // "Entity -> DTO" Method
    private MemberEditForm createMemberEditFrom(Member member) {
        MemberEditForm memberEditForm = new MemberEditForm();
        memberEditForm.setId(member.getId());
        memberEditForm.setName(member.getName());
        memberEditForm.setNickname(member.getNickname());
        memberEditForm.setDesiredJob(member.getDesiredJob());
        memberEditForm.setIntroduce(member.getIntroduce());
        memberEditForm.setLinks(member.getLinks());
        memberEditForm.setImage(member.getImage());
        return memberEditForm;
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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 0-4 사이 요청 (이틀전까지의 데이터)
        if (LocalDateTime.now().getHour() < 4) {
            String end = LocalDateTime.now().minusDays(1).format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(8).format(dateTimeFormatter);
            posts = postService.getLast7(member.getId(), start, end);
            for (int i = 8; i > 1; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        } else { // 이외
            String end = LocalDateTime.now().format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(7).format(dateTimeFormatter);
            posts = postService.getLast7(member.getId(), start, end);
            for (int i = 7; i > 0; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        }
        for (Post post : posts) {
//            if (post.getTimes().size() % 2 != 0)  continue;
            Times time = getTimes(post.getTimes());
            int today = (time.getHour() * 60 + time.getMin());
            Times totalTimes = new Times(today);
            dayTimes.put(post.getCreateTime().getDayOfMonth(), totalTimes);
        }

        member.setWorkCnt(posts.size());

        return dayTimes;
    }

    private Times getAllTimes(Member member) {
        List<Post> posts = member.getPosts();
        int total = 0;
        for (Post post : posts) {
//            if (post.getTimes().size() % 2 != 0)  continue;
            Times times = getTimes(post.getTimes());
            total += times.getHour() * 60 + times.getMin();
        }

        return new Times(total);
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
        } else {
            resultTime = new Times(0);
        }
        return resultTime;
    }

    @Data
    static class GoalDto {

        private int dayHour;
        private int dayMin;
        private int weekHour;
        private int weekMin;

        public GoalDto(Goal goal) {
            int day = goal.getDayGoalTimes();
            this.dayHour = Math.floorDiv(day, 60);
            this.dayMin = day % 60;
            int week = goal.getWeekGoalTimes();
            this.weekHour = Math.floorDiv(week, 60);
            this.weekMin = week % 60;
        }

        public GoalDto() {
        }
    }

    @Data
    static class AllStatic {
        /*전체 공부시간
        일주일 총 공부시간
        일주일 평균 공부시간
        한달 총 공부시간
        한달 평균 공부시간
        한달 출석률
        목표 달성률
        공부 효율
        공부 랭크*/
        private Times AllTimes;
        private Times weekAllTimes;
        private Times weekAvgTimes;
        private Times monthAllTimes;
        private Times monthAvgTimes;

        private int nowMonthAttRate;

        private int monthAttendanceCnt;
        private int monthDate;
        private int monthAttendanceRate;

        private int goalAttainmentToday;
        private int goalAttainmentWeek;

        private int studyEfficiency;
        private int studyRankRate;
    }

    @Data
    @AllArgsConstructor
    static class ForCalender {

        private int year;
        private String month;
        private int startDay;

    }


}
