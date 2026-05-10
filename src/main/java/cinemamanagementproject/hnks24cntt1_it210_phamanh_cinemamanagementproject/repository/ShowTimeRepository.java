package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;


import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ShowTimeResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.ShowStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// repository/ShowTimeRepository.java
@Repository
public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    // Tìm các suất chiếu của cùng phòng có thời gian giao nhau
    // Dùng để kiểm tra xung đột khi tạo mới
    @Query("""
        SELECT s FROM ShowTime s
        WHERE s.room.roomId = :roomId
          AND s.showId <> :excludeId
          AND s.startAt < :newEnd
          AND s.endedAt > :newStart
    """)
    List<ShowTime> findConflicts(
            @Param("roomId")    Long roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd")    LocalDateTime newEnd,
            @Param("excludeId") Long excludeId    // -1 khi tạo mới
    );

    // Lấy tất cả suất chiếu kèm movie + room (tránh N+1)
    @Query("SELECT s FROM ShowTime s JOIN FETCH s.movie JOIN FETCH s.room ORDER BY s.startAt DESC")
    List<ShowTime> findAllWithDetails();

    // Lấy tất cả suất chiếu và sắp xếp theo thời gian mới nhất
    @Query("SELECT s FROM ShowTime s ORDER BY s.startAt DESC")
    List<ShowTime> findAllOrderByStartAtDesc();

    // Lấy suất chiếu đang diễn ra
    @Query("SELECT s FROM ShowTime s JOIN FETCH s.movie JOIN FETCH s.room WHERE :now >= s.startAt AND :now <= s.endedAt")
    List<ShowTime> findNowShowing(@Param("now") LocalDateTime now);

    // Lấy suất chiếu sắp diễn ra (Đảm bảo có JOIN FETCH s.movie ...)
    @Query("SELECT s FROM ShowTime s JOIN FETCH s.movie JOIN FETCH s.room WHERE s.startAt > :now")
    List<ShowTime> findUpcoming(@Param("now") LocalDateTime now);


    // Cập nhật suất chiếu theo thơif gian thực và thời gia suất chiếu kết thúc
    @Query("""
        update ShowTime s set s.status = :status where s.endedAt < :now and s.status != :newStatus
        """)
    void updateShowtimeStatus(@Param("now") LocalDateTime now, @Param("newStatus")ShowStatus status);


}

