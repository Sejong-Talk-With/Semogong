package Talk_with.semogong.repository;

import Talk_with.semogong.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostNativeRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * " +
            "FROM post p " +
            "where p.member_id = :m_id " +
            "and p.create_time > DATE_SUB(CONVERT_TZ( NOW(),'SYSTEM','Asia/Seoul'), INTERVAL 7+ :off DAY) " +
            "and p.create_time < DATE_SUB(CONVERT_TZ( NOW(),'SYSTEM','Asia/Seoul'), INTERVAL :off DAY);", nativeQuery = true)
    List<Post> getLast7(@Param(value = "m_id") Long id, @Param(value = "off") int offset);

}
