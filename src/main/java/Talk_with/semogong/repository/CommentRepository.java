package Talk_with.semogong.repository;

import Talk_with.semogong.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    // DI
    private final EntityManager em;

    // 댓글 단건 조회
    public Comment findOne(Long id) {
        return em.find(Comment.class, id);
    }

    // 댓글 저장
    public void save(Comment comment){
        em.persist(comment);
    }

    // 댓글 수정
    public void edit(Long id, String content) {
        Comment comment = em.find(Comment.class, id);
        comment.editContent(content);
    }

    // 댓글 삭제
    public void deleteOne(Long id) {
        Comment comment = em.find(Comment.class, id);
        em.remove(comment);
    }


}
