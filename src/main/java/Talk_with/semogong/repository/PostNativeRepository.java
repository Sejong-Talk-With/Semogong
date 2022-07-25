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
            "and p.create_time BETWEEN :start_date and :end_date ;", nativeQuery = true)
    List<Post> getLast7(@Param(value = "m_id") Long id, @Param(value="start_date") String start, @Param(value = "end_date") String end);

    @Query(value = "SELECT * " +
            "FROM post p " +
            "where p.member_id = :m_id " +
            "and MONTH(p.create_time) = :request_month ;", nativeQuery = true)
    List<Post> getMonthPost(@Param(value = "m_id") Long id, @Param(value="request_month") int month);

    @Query(value = "SELECT * " +
            "FROM post p " +
            "where DATE_FORMAT(p.create_time , '%Y-%m-%d') = :date " +
            "ORDER BY p.create_time DESC " +
            "LIMIT 12 OFFSET :offset ;", nativeQuery = true)
    List<Post> getDatePost(@Param(value="date") String date, @Param(value = "offset") int offset);

}
