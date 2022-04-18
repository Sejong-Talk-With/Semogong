package Talk_with.semogong.controller;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.S3Service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpSession;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {


    private final MemberService memberService;
    private final S3Service s3Service;

    // 회원가입 폼
    @GetMapping("/members/signup")
    public String signUpForm(Model model) {
        log.info("signup");
        model.addAttribute("memberForm", new MemberForm());
        return "member/createMemberForm";
    }

    // 회원가입 진행
    @PostMapping("/members/signup")
    public String signUp(@Valid MemberForm memberForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.info("found Null, re-joining");
            return "member/createMemberForm"; }
        Member member = Member.createMember(memberForm.getLoginId(), memberForm.getPassword(), memberForm.getName(),memberForm.getNickname(),memberForm.getDesiredJob(), null,memberForm.getIntroduce(),null ,null);
        member.setRole("USER");
        memberService.save(member);
        return "redirect:/";
    }

    @GetMapping("/members/edit/{id}")
    public String memberEditForm(@PathVariable("id") Long id, Model model) {
        Member member = memberService.findOne(id);
        MemberEditForm memberEditForm = createMemberEditFrom(member);
        model.addAttribute("memberEditForm",memberEditForm);
        return "member/editMemberForm";
    }

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

    @PostMapping("/members/edit/{id}/img")
    public void memberImgEdit(@PathVariable("id") Long id, @RequestParam("file") MultipartFile[] files) throws IOException {
        MultipartFile file = files[0];
        Image image = s3Service.upload(file);
        memberService.editMemberImg(id, image);
    }

    @GetMapping("/members/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; //주소 요청으로 변경
    }

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
