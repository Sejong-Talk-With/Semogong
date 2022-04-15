package Talk_with.semogong.service;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.auth.MyUserDetail;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.form.MemberForm;
import Talk_with.semogong.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Long save(Member member) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
        return member.getId();
    }

    public Member findOne(Long id){
        return memberRepository.findOne(id);
    }

    public List<Member> findAll(){
        return memberRepository.findAll();
    }

    public Member findByName(String name){
        return memberRepository.findByName(name);
    }

    public Member findByLoginId(String loginId){
        return memberRepository.findUserByEmail(loginId);
    }


    public StudyState checkState(Long memberId) {
        Member curr_member = memberRepository.findOne(memberId);
        return curr_member.getState();
    }

    public void changeState(Long memberId, StudyState state) {
        Member curr_member = memberRepository.findOne(memberId);
        curr_member.changeState(state);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //여기서 받은 유저 패스워드와 비교하여 로그인 인증
        Member user = memberRepository.findUserByEmail(email);
        return new MyUserDetail(user);
    }

    public void editMember(Long id, MemberForm memberForm) {
        Member member = memberRepository.findOne(id);
        member.edit(memberForm);
    }

    public void editMemberImg(Long id, Image image) {
        Member member = memberRepository.findOne(id);
        member.setImage(image);
    }

//    public List<String> findLinks(Long memberId) {
//        memberRepository.findLinks(memberId);
//    }
}
