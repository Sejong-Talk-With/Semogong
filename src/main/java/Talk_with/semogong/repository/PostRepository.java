package Talk_with.semogong.repository;


import Talk_with.semogong.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    // DI : Entity Manger
    private final EntityManager em;

    // 게시글 저장
    public void save(Post post){
        em.persist(post);
    }

    // Post id를 통한 게시글 단일 조회
    public Post findOne(Long id){
        return em.find(Post.class, id);
    }

    // 게시글 다건 조회
    public List<Post> findAll() {
        return em.createQuery("select p from Post p order by p.createTime DESC", Post.class).getResultList();
    }

    // Member Id에 따른 최근 게시글 단일 조회
    public Optional<Post> findByMember(Long memberId) {
        return em.createQuery("select p from Post p join p.member m where m.id = :id order by p.createTime DESC", Post.class)
                .setParameter("id", memberId).setMaxResults(1)
                .getResultList().stream().findAny();
    }

    // Member Id에 따른 최근 게시글 단일 조회
    public List<Post> findByMemberWithPaging(Long memberId, Integer offset) {
        return em.createQuery("select p from Post p join fetch p.member m where m.id = :id order by p.createTime DESC", Post.class)
                .setParameter("id", memberId)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    // 게시글 페이징 다건 조회
    public List<Post> findByPaging(Integer offset) {
        return em.createQuery("select p from Post p order by p.createTime DESC", Post.class)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public List<Post> findByTitle(String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p where p.title like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public List<Post> findByContent(String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p where p.content like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public List<Post> findByJob(String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p join p.member m where m.desiredJob like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public List<Post> findByTitleMy(Long memberId, String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p join p.member m where m.id = :id and p.title like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("id", memberId)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    public List<Post> findByContentMy(Long memberId, String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p join p.member m where m.id = :id and p.content like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("id", memberId)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }

    // 게시글 단일 삭제
    public void deleteOne(Post post) {
        em.remove(post);
    }


    public List<Post> findSearchByMember(String searchKeyword, Integer offset) {
        return em.createQuery("select p from Post p join p.member m where m.name like :searchKeyword order by p.createTime DESC", Post.class)
                .setParameter("searchKeyword", searchKeyword)
                .setFirstResult(Integer.parseInt(offset.toString()))
                .setMaxResults(12)
                .getResultList();
    }
}
