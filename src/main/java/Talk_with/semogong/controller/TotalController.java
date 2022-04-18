package Talk_with.semogong.controller;


import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.form.PostForm;
import Talk_with.semogong.service.MemberService;
import Talk_with.semogong.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TotalController {

    private final PostService postService;
    private final MemberService memberService;


    // 공부중 클릭
    @GetMapping("/studying")
    public String studying(Authentication authentication){

        Long memberId = getLoginMemberId(authentication);
        StudyState state = memberService.checkState(memberId); // 현재 로그인된 회원의 상태를 조회

        // Case #1 (사용자의 현상태가 공부완료 -> 즉, 공부 시작)
        if (state == StudyState.END) {

            Optional<Post> optionalPost = postService.getRecentPost(memberId);

            // End -> Studying 넘어갈 때, 이전 글을 이어 쓰냐, 새로 쓰냐 판단
            // 사용자가 글을 쓴 기록이 있는지 확인 -> 맨 처음 글을 작성하는 멤버일 경우 넘어감.
            if (optionalPost.isPresent()) {
                Post post = optionalPost.get();
                LocalDate createDate = post.getCreateTime().toLocalDate();
                LocalDateTime nowDateTime = LocalDateTime.now();

                // 최신 글이 해당 날짜에 작성한 글인지 판단
                if (createDate.isEqual(nowDateTime.toLocalDate())) { // yes : 그 글 이어작성
                    memberService.changeState(memberId, StudyState.STUDYING);
                    postService.changeState(post.getId(), StudyState.STUDYING);
                    postService.addTime(memberId, LocalDateTime.now());
                    return "redirect:/";
                } else { // no
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH");
                    int nowTime = Integer.parseInt(nowDateTime.format(timeFormatter));
                    // 현재 시점이 4시 이전
                    if (nowTime < 4) {
                        Period period = Period.between(createDate, nowDateTime.toLocalDate());
                        // 그 전날 작성한 글이 존재하는 지 확인
                        if (period.getDays() == 1) { // yes : 해당 글 이어 작성
                            memberService.changeState(memberId, StudyState.STUDYING);
                            postService.changeState(post.getId(), StudyState.STUDYING);
                            postService.addTime(memberId, LocalDateTime.now());
                            return "redirect:/";
                        }
                        // no : 새글 작성
                    }
                    // 현재 시점이 4시 이후 -> 걍 새 글 작성
                }
            }

            memberService.changeState(memberId, StudyState.STUDYING); // posting 후 state change 필요 (오류 처리해야 됨)
            PostForm postForm = new PostForm();
            postForm.setTime(LocalDateTime.now());
            Long postId = postService.post(memberId, new PostForm());
            return "redirect:/posts/edit/" + postId.toString();
        }

        Long postId = postService.getRecentPost(memberId).get().getId();

        // Case #2 (사용자의 현상태가 휴식 중 -> 즉, 공부 재시작)
        if (state == StudyState.BREAKING) {
            memberService.changeState(memberId, StudyState.STUDYING);
            postService.changeState(postId, StudyState.STUDYING);
            postService.addTime(memberId, LocalDateTime.now());
            return "redirect:/";
        }

        // Case #3 (사용자의 현상태가 공부 중 -> 오류 던지기)
        return "redirect:/";
    }

    // 휴식중 클릭
    @GetMapping("/breaking")
    public String breaking(Authentication authentication){

        Long memberId = getLoginMemberId(authentication);
        StudyState state = memberService.checkState(memberId); // 현재 로그인된 회원의 상태를 조회

        // Case #1 (사용자의 현상태가 공부 중 -> 즉, 휴식 시작)
        if (state == StudyState.STUDYING) {
            Long postId = postService.getRecentPost(memberId).get().getId();
            memberService.changeState(memberId, StudyState.BREAKING);
            postService.changeState(postId, StudyState.BREAKING);
            postService.addTime(memberId, LocalDateTime.now());
            return "redirect:/";
        }

        // Case #2 (사용자의 현상태가 휴식 중, 공부 완료 -> 오류 던지기)
        return "redirect:/";
    }

    // 완료 클릭
    @GetMapping("/end")
    public String end(Authentication authentication){

        Long memberId = getLoginMemberId(authentication);
        StudyState state = memberService.checkState(memberId); // 현재 로그인된 회원의 상태를 조회

        // Case #1 (사용자의 현상태가 공부 완료 -> 오류)
        if (state == StudyState.END) {
            return "redirect:/";
        }

        Long postId = postService.getRecentPost(memberId).get().getId();
        // Case #2 (사용자의 현상태가 공부 중 -> 즉, 공부 완료)
        if (state == StudyState.STUDYING) {
            memberService.changeState(memberId, StudyState.END);
            postService.changeState(postId, StudyState.END);
            postService.addTime(memberId, LocalDateTime.now());
            return "redirect:/";
        }

        // Case #3 (사용자의 현상태가 휴식 중 -> 그대로 상태 변경 후 종료)
        else {
            memberService.changeState(memberId, StudyState.END);
            postService.changeState(postId, StudyState.END);
            return "redirect:/";
        }

    }

    private Long getLoginMemberId(Authentication authentication) {
        MyUserDetail userDetail =  (MyUserDetail) authentication.getPrincipal();  //userDetail 객체를 가져옴 (로그인 되어 있는 놈)
        String loginId = userDetail.getEmail();
        return memberService.findByLoginId(loginId).getId(); // "박승일"로 로그인 했다고 가정, 해당 로그인된 회원의 ID를 가져옴
    }

    public boolean checkToday(Post post) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createTime = post.getCreateTime().format(formatter);
        String now = LocalDateTime.now().format(formatter);
        return createTime.equals(now);
    }
}
