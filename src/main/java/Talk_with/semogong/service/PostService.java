package Talk_with.semogong.service;

import Talk_with.semogong.domain.att.Image;
import Talk_with.semogong.domain.Post;
import Talk_with.semogong.domain.att.StudyState;
import Talk_with.semogong.domain.form.PostEditForm;
import Talk_with.semogong.repository.MemberRepository;
import Talk_with.semogong.repository.PostNativeRepository;
import Talk_with.semogong.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostNativeRepository postNativeRepository;

    // 게시글 저장
    public Long save(Long memberId, LocalDateTime createTime) {
        Post post = Post.createPost(memberRepository.findOne(memberId), createTime); // 공부중 click했을 때의 시각을 post의 createTime으로 설정
        postRepository.save(post);
        return post.getId();
    }

    // 게시글 수정
    public void edit(PostEditForm postEditForm) {
        Post post = postRepository.findOne(postEditForm.getId());
        post.edit(postEditForm);
    }

    // post id를 통한 게시글 단건 조회
    @Transactional(readOnly = true)
    public Post findOne(Long id) {
        return postRepository.findOne(id);
    }

    // 게시글 다건 조회
    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // 상태 변경에 따른 post 시간 추가
    public void addTime(Long memberId, LocalDateTime time) {
        Post post = postRepository.findByMember(memberId).get(); // memberId 에 따른 Post
        post.addTime(time);
    }

    // 게시글 페이징 조회
    @Transactional(readOnly = true)
    public List<Post> findByPage(Integer offset) {
        return postRepository.findByPaging(offset);
    }

    // 게시글 상태 변경
    public void changeState(Long postId, StudyState state) {
        Post post = postRepository.findOne(postId);
        post.editState(state);
    }

    // member id를 통한 최근 게시글 조회
    @Transactional(readOnly = true)
    public Optional<Post> getRecentPost(Long memberId) {
        return postRepository.findByMember(memberId);
    }

    public List<Post> getLast7(Long id, String start, String end) {
        return postNativeRepository.getBetween(id, start, end);
    }

    public List<Post> getMonthPosts(Long id, int month) {
        return postNativeRepository.getMonthPost(id, month);
    }

    public List<Post> getTodayPosts(String date, int offset) {
        return postNativeRepository.getDatePost(date, offset);
    }

    public List<Post> getMemberPosts(Long id, int offset) {
        return postRepository.findByMemberWithPaging(id, offset);
    }

    // 게시글 이미지 업로드 및 변경
    public void editPostImg(Long id, Image image) {
        Post post = postRepository.findOne(id);
        post.setImage(image);
    }

    @Transactional(readOnly = true)
    public List<Post> findBySearch(String selected, String content, int offset) {
        String searchKeyword = '%' + content + '%'; // .toUpperCase(Locale.ROOT)
        if (selected.equals("title")) {
            return postRepository.findByTitle(searchKeyword, offset);
        } else if (selected.equals("writer")) {
            return postRepository.findSearchByMember(searchKeyword, offset);
        } else if (selected.equals("desiredJob")) {
            return postRepository.findByJob(searchKeyword, offset);
        } else {
            return postRepository.findByContent(searchKeyword, offset);
        }
    }

    @Transactional(readOnly = true)
    public List<Post> findByTodaySearch(String selected, String content, String date, int offset) {
        String searchKeyword = '%' + content + '%'; // .toUpperCase(Locale.ROOT)

        if (selected.equals("title")) {
            return postNativeRepository.findByTitleToday(searchKeyword, date, offset);
        } else if (selected.equals("writer")) {
            return postNativeRepository.findByWriterToday(searchKeyword, date, offset);
        } else if (selected.equals("desiredJob")) {
            return postNativeRepository.findByJobToday(searchKeyword, date, offset);
        } else {
            return postNativeRepository.findByContentToday(searchKeyword, date, offset);
        }
    }

    @Transactional(readOnly = true)
    public List<Post> findBySearchMy(Long memberId, String selected, String content, int offset) {
        String searchKeyword = '%' + content + '%'; // .toUpperCase(Locale.ROOT)

        if (selected.equals("title")) {
            return postRepository.findByTitleMy(memberId, searchKeyword, offset);
        } else {
            return postRepository.findByContentMy(memberId, searchKeyword, offset);
        }
    }


    // 게시글 삭제
    public void deletePost(Post post) {
        postRepository.deleteOne(post);
    }
}
