package Talk_with.semogong.service;

import Talk_with.semogong.domain.Comment;
import Talk_with.semogong.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    // DI
    private final CommentRepository commentRepository;

    // 댓글 저장 (리포지토리 단순 위임)
    public Long save(Comment comment){
        commentRepository.save(comment);
        return comment.getId();
    }

    public void edit(Long commentId, String content) {
        commentRepository.edit(commentId, content);
    }

    // 댓글 삭제 (리포지토리 단순 위임)
    public void deleteComment(Long id) {
        commentRepository.deleteOne(id);
    }

    public Comment findOne(Long commentId) {
        return commentRepository.findOne(commentId);
    }
}
