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

    private final CommentRepository commentRepository;

    public Long save(Comment comment){
        commentRepository.save(comment);
        return comment.getId();
    }

    public void deleteComment(Long id) {
        commentRepository.deleteOne(id);
    }
}
