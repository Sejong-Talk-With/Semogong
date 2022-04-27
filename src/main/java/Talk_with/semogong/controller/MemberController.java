package Talk_with.semogong.controller;

import Talk_with.semogong.domain.att.DesiredJob;
import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {


    private final MemberService memberService;
    private final S3Service s3Service;

    // 회원가입 폼
    @GetMapping("/members/signup")
    public String signUpForm(Model model) {
        List<String> jobList = DesiredJob.getJobList();
        log.info("signup");
        model.addAttribute("memberForm", new MemberForm());
        model.addAttribute("jobs", jobList);
        return "member/createMemberForm";
    }

    // 회원가입 진행
    @PostMapping("/members/signup")
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
    @GetMapping("/members/edit/{id}")
    public String memberEditForm(@PathVariable("id") Long id, Model model) {
        List<String> jobList = DesiredJob.getJobList();
        Member member = memberService.findOne(id);
        MemberEditForm memberEditForm = createMemberEditFrom(member);
        model.addAttribute("memberEditForm",memberEditForm);
        model.addAttribute("jobs", jobList);
        return "member/editMemberForm";
    }

    // 회원 정보 수정 (이미지 업로드 및 수정 폼 포함)
    @PostMapping("/members/edit/{id}")
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
    @PostMapping("/members/edit/{id}/img")
    public void memberImgEdit(@PathVariable("id") Long id, @RequestParam("file") MultipartFile[] files) throws IOException {
        MultipartFile file = files[0];
        Image image = s3Service.upload(file);
        memberService.editMemberImg(id, image);
    }

    // 회원 로그아웃
    @GetMapping("/members/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; //주소 요청으로 변경
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




}
