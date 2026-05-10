package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Tìm tất cả vé dựa trên ID của suất chiếu
    List<Ticket> findByShowTime_ShowId(Long showId);

    // (Tùy chọn) Nếu bạn muốn kiểm tra nhanh một ghế cụ thể đã có vé chưa
    boolean existsByShowTime_ShowIdAndSeat_SeatId(Long showId, Long seatId);

    @Query("""
                SELECT COUNT(t) > 0 FROM Ticket t
                WHERE t.showTime.showId = :showId
                AND t.seat.seatId = :seatId
                AND t.booking.status IN (:statuses)
            """)
    boolean existsActiveTicket(
            @Param("showId") Long showId,
            @Param("seatId") Long seatId,
            @Param("statuses") List<BookingStatus> statuses
    );

    @Query("""
                SELECT t FROM Ticket t
                WHERE t.showTime.showId = :showId
                AND t.booking.status IN :statuses
            """)
    List<Ticket> findActiveByShowId(
            @Param("showId") Long showId,
            @Param("statuses") List<BookingStatus> statuses
    );

    void deleteByBookingBookingIdAndSeatSeatId(Long bookingBookingId, Long seatSeatId);

    @Query("""
        SELECT COUNT(t) > 0 FROM Ticket t
        WHERE t.showTime.showId = :showId
          AND t.seat.seatId IN :seatIds
          AND t.booking.status IN :statuses
    """)
    boolean existsAnyActiveTicket(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("statuses") List<BookingStatus> statuses
    );
}
