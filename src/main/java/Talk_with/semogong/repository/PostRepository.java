package Talk_with.semogong.repository;


import Talk_with.semogong.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final EntityManager em;

    public void save(Post post){
        em.persist(post);
    }

    public Post findOne(Long id){
        return em.find(Post.class, id);
    }

    public List<Post> findAll() {
        return em.createQuery("select p from Post p order by p.createTime DESC", Post.class).getResultList();
    }

    public Post findByMember(Long memberId) {
        return em.createQuery("select p from Post p join p.member m where m.id = :id order by p.createTime DESC", Post.class)
                .setParameter("id", memberId).setMaxResults(1).getSingleResult();
    }

    public List<Post> findByPaging(Integer offset) {
        return em.createQuery("select p from Post p order by p.createTime DESC", Post.class)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public void deleteOne(Post post) {
        em.remove(post);
    }
}
