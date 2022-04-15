package Talk_with.semogong.repository;

import Talk_with.semogong.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member){
        em.persist(member);
    }

    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m",Member.class).getResultList();
    }

    public Member findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class) // name을 parameter로 바인딩
                .setParameter("name", name)
                .getSingleResult();
    }

    public Member findUserByEmail(String login_id){
        return em.createQuery("select m from Member as m where m.loginId = :login_id", Member.class)
                .setParameter("login_id", login_id).getSingleResult();
    }
}
