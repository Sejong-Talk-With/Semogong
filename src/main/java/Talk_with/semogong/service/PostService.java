package Talk_with.semogong.service;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.StudyState;
import Talk_with.semogong.domain.form.PostEditForm;
import Talk_with.semogong.domain.form.PostForm;
import Talk_with.semogong.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final MemberService memberService;
    private final PostRepository postRepository;

    public void save(Post post){
        postRepository.save(post);
    }

    public Long post(Long memberId, PostForm postForm) {
        Post post = Post.createPost(memberService.findOne(memberId), postForm.getTitle(), postForm.getIntroduce(), postForm.getContent(), postForm.getHtml(), LocalDateTime.now()); // 공부중 click했을 때의 시각이 아닌, 게시물을 post 했을 때를 공부 시작으로 기준 잡음. -> 수정 필요
        postRepository.save(post);
        return post.getId();
    }

    public void edit(PostEditForm postEditForm) {
        Post post = postRepository.findOne(postEditForm.getId());
        post.edit(postEditForm);
    }

    public Post findOne(Long id) {
        return postRepository.findOne(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // 상태 클릭에 따른 post 시간 변경
    public void addTime(Long memberId, LocalDateTime time) {
        Post post = postRepository.findByMember(memberId); // memberId 에 따른 Post
        post.addTime(time);
    }

    public List<Post> findByPage(Integer offset) {
        return postRepository.findByPaging(offset);
    }

    public void changeState(Long postId, StudyState state) {
        Post post = postRepository.findOne(postId);
        post.editState(state);
    }

    public Post getRecentPost(Long memberId) {
        return postRepository.findByMember(memberId);
    }

    public void editPostImg(Long id, Image image) {
        Post post = postRepository.findOne(id);
        post.setImage(image);
    }

    public void deletePost(Post post) {
        postRepository.deleteOne(post);
    }
}
