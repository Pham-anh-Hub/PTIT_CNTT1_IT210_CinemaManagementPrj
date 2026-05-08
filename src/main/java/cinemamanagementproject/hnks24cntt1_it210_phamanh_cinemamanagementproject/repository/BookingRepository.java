package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.tickets t " +        // Join sang danh sách vé
            "JOIN FETCH t.showTime s " +       // Từ vé join sang suất chiếu
            "JOIN FETCH s.movie m " +          // Từ suất chiếu lấy phim
            "JOIN FETCH s.room r " +           // Từ suất chiếu lấy phòng
            "WHERE b.user.userId = :userId " +
            "ORDER BY b.bookedAt DESC")        // Lưu ý: Dùng b.bookedAt thay vì b.createdAt theo SQL của bạn
    List<Booking> findHistoryByUserId(@Param("userId") Long userId);
}
