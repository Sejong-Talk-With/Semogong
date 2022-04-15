package Talk_with.semogong.repository;

import Talk_with.semogong.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final EntityManager em;

    public void save(Comment comment){
        em.persist(comment);
    }

    public void deleteOne(Long id) {
        Comment comment = em.find(Comment.class, id);
        em.remove(comment);
    }
}
