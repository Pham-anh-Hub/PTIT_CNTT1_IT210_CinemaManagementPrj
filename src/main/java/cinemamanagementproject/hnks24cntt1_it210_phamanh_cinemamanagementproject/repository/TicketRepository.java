package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Tìm tất cả vé dựa trên ID của suất chiếu
    List<Ticket> findByShowTime_ShowId(Long showId);
    // (Tùy chọn) Nếu bạn muốn kiểm tra nhanh một ghế cụ thể đã có vé chưa
    boolean existsByShowTime_ShowIdAndSeat_SeatId(Long showId, Long seatId);

    void deleteByBookingBookingIdAndSeatSeatId(Long bookingBookingId, Long seatSeatId);
}
