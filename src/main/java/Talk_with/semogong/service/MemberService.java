package Talk_with.semogong.service;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.form.MemberEditForm;
import Talk_with.semogong.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 회원 저장 (가입 비밀번호를 인코딩한 채로 DB에 저장)
    public Long save(Member member) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
        return member.getId();
    }

    // id를 통한 회원 조회 (리포지토리 단순 위임)
    @Transactional(readOnly = true)
    public Member findOne(Long id){
        return memberRepository.findOne(id);
    }

    // 전체 회원 조회 (비지니스 로직 사용)
    @Transactional(readOnly = true)
    public List<Member> findAll(){
        List<Member> members = memberRepository.findAll();
        List<Member> sortedMembers = new ArrayList<>();
        List<StudyState> orders = new ArrayList<>();
        orders.add(StudyState.STUDYING);
        orders.add(StudyState.BREAKING);
        orders.add(StudyState.END);

        // 비지니스 로직 : Studying, Breaking, End 순으로 정렬하여 반환.
        for (StudyState state : orders)
            for (Member member : members)
                if (member.getState() == state) sortedMembers.add(member);
        return sortedMembers;
    }

    // 이름을 통해 회원 조회 (레포지토리 단순 위임, 아직 사용 X)
    @Transactional(readOnly = true)
    public Member findByName(String name){
        return memberRepository.findByName(name);
    }

    // LoginId를 통해 회원 조회 (레포지토리 단순 위임)
    @Transactional(readOnly = true)
    public Member findByLoginId(String loginId){
        return memberRepository.findByLoginId(loginId);
    }

    // 현재 회원의 state 조회
    @Transactional(readOnly = true)
    public StudyState checkState(Long memberId) {
        Member curr_member = memberRepository.findOne(memberId);
        return curr_member.getState();
    }

    // 현재 회원의 state 변경
    public void changeState(Long memberId, StudyState state) {
        Member curr_member = memberRepository.findOne(memberId);
        curr_member.changeState(state);
    }

    // 로그인 인증 (Spring Security)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //여기서 받은 유저 패스워드와 비교하여 로그인 인증
        Member user = memberRepository.findByLoginId(email);
        return new MyUserDetail(user);
    }

    // 회원 정보 수정 (수정 로직은 엔티티 자체 메서드로 진행)
    public void editMember(Long id, MemberEditForm memberEditForm) {
        Member member = memberRepository.findOne(id);
        member.edit(memberEditForm);
    }

    // 회원 이미지 업로드
    public void editMemberImg(Long id, Image image) {
        Member member = memberRepository.findOne(id);
        member.setImage(image);
    }

}
