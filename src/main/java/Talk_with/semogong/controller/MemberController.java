package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.DesiredJob;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.att.Times;
import Talk_with.semogong.domain.dto.MemberDto;
import Talk_with.semogong.domain.form.LoginForm;
import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.repository.PostNativeRepository;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import Talk_with.semogong.service.S3Service;
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

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {


    private final MemberService memberService;
    private final S3Service s3Service;
    private final PostNativeRepository postNativeRepository;

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
            return "member/createMemberForm"; }
        Member member = Member.createMember(memberForm.getLoginId(), memberForm.getPassword(), memberForm.getName(),memberForm.getNickname(),memberForm.getDesiredJob(), null,memberForm.getIntroduce(),null ,null);
        member.setRole("USER");
        memberService.save(member);
        return "redirect:/";
    }

    // 회원 정보 수정 폼
    @GetMapping("/edit/{id}")
    public String memberEditForm(@PathVariable("id") Long id, Model model) {
        List<String> jobList = DesiredJob.getJobList();
        Member member = memberService.findOne(id);
        MemberEditForm memberEditForm = createMemberEditFrom(member);
        model.addAttribute("memberEditForm",memberEditForm);
        model.addAttribute("jobs", jobList);
        return "member/editMemberForm";
    }

    // 회원 정보 수정 (이미지 업로드 및 수정 폼 포함)
    @PostMapping("/edit/{id}")
    public String memberEdit(@PathVariable("id") Long id, @Valid MemberEditForm memberEditForm, BindingResult result){
        List<String> links = memberEditForm.getLinks(); while (links.remove("")){ }
        memberEditForm.setLinks(links);
        if (result.hasErrors()) {
            log.info("found Null, re-editing");
            return "member/editMemberForm";
        }
        memberService.editMember(id, memberEditForm);
        return "redirect:/";
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
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletRequest request) {


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
        session.setAttribute("loginMember", loginMember.get().getId());
        return "redirect:"+ redirectURL;
    }

    // 회원 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; //주소 요청으로 변경
    }

    @GetMapping("/my-page")
    public String myPage(@SessionAttribute(name = "loginMember", required = false) Long loginMemberId, Model model) {
        if (loginMemberId == null) {
            return "redirect:/";
        }
        Member oriMember = memberService.findOne(loginMemberId);
        MemberInfo member = new MemberInfo(oriMember);
        List<Integer> days = getDays(LocalDateTime.now());
        Map<Integer, Times> staticsData = getStaticsData(member);
        member.setTime(getAllTimes(oriMember));

        model.addAttribute("staticDays", days);
        model.addAttribute("nav", "myPage");
        model.addAttribute("member", member);
        model.addAttribute("staticData", staticsData);
        return "member/memberDetail";
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

    @Data
    static class MemberInfo {

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
        private int workCnt;

        public MemberInfo(Member member) {
            this.setId(member.getId());
            this.setLoginId(member.getLoginId());
            this.setName(member.getName());
            this.setNickname(member.getNickname());
            this.setDesiredJob(member.getDesiredJob());
            this.setIntroduce(member.getIntroduce());
            this.setLinks(member.getLinks());
            this.setImage(member.getImage());
            this.setState(member.getState());
        }
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

    private Map<Integer, Times> getStaticsData(MemberInfo member) {

        List<Post> posts;
        Map<Integer, Times> dayTimes = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 0-4 사이 요청 (이틀전까지의 데이터)
        if (LocalDateTime.now().getHour() < 4) {
            String end = LocalDateTime.now().minusDays(1).format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(8).format(dateTimeFormatter);
            posts = postNativeRepository.getLast7(member.getId(),start,end);
            for (int i = 8; i > 1; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        } else { // 이외
            String end = LocalDateTime.now().format(dateTimeFormatter);
            String start = LocalDateTime.now().minusDays(7).format(dateTimeFormatter);
            posts = postNativeRepository.getLast7(member.getId(),start, end);
            for (int i = 7; i > 0; i--) {
                dayTimes.put(LocalDateTime.now().minusDays(i).getDayOfMonth(), new Times(0));
            }
        }
        for (Post post : posts) {
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
            if (post.getTimes().size() % 2 != 0) continue;
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
        }
        return resultTime;
    }





}
