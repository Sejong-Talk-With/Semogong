package Talk_with.semogong.repository;

import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    // 회원 저장
    public void save(Member member){
        em.persist(member);
    }

    // id를 통해서 회원 하나 조회
    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    // 모든 회원 조회
    public List<Member> findAll(){
        return em.createQuery("select m from Member m",Member.class).getResultList();
    }

    // 이름을 통해 회원 조회
    public Member findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class) // name을 parameter로 바인딩
                .setParameter("name", name)
                .getSingleResult();
    }

    // LoginId로 회원 조회
    public Optional<Member> findByLoginId(String loginId){
        return em.createQuery("select m from Member as m where m.loginId = :login_id", Member.class)
                .setParameter("login_id", loginId)
                .getResultList().stream().findAny();
    }
}
