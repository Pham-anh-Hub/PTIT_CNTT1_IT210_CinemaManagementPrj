package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.tickets t " +
            "JOIN FETCH t.showTime s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.room r " +
            "WHERE b.user.userId = :userId " +
            "ORDER BY b.bookedAt DESC")
    List<Booking> findHistoryByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT DISTINCT b FROM Booking b
                JOIN FETCH b.tickets t
                JOIN FETCH t.showTime st
                JOIN FETCH st.movie m
                JOIN FETCH st.room
                JOIN FETCH t.seat
                WHERE b.user.userId = :userId
                AND (:status IS NULL OR b.status = :status)
                AND (:movieTitle IS NULL OR LOWER(m.movieTitle) LIKE LOWER(CONCAT('%', :movieTitle, '%')))
                AND (:fromDate IS NULL OR b.bookedAt >= :fromDate)
                AND (:toDate IS NULL OR b.bookedAt <= :toDate)
                ORDER BY b.bookedAt DESC
            """)
    List<Booking> findHistoryWithFilters(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status,
            @Param("movieTitle") String movieTitle,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
                SELECT b FROM Booking b
                JOIN FETCH b.tickets t
                JOIN FETCH t.showTime st
                JOIN FETCH st.movie
                JOIN FETCH st.room
                JOIN FETCH t.seat
                WHERE b.bookingId = :id
            """)
    Optional<Booking> findByIdWithTickets(@Param("id") Long id);

    @Query("""
                SELECT DISTINCT b FROM Booking b
                JOIN FETCH b.tickets t
                JOIN FETCH t.showTime
                WHERE b.status = :status
            """)
    List<Booking> findPendingWithShowTime(@Param("status") BookingStatus status);


    @Query("""
                SELECT DISTINCT b FROM Booking b
                JOIN FETCH b.tickets t
                JOIN FETCH t.showTime st
                JOIN FETCH st.movie
                JOIN FETCH st.room
                JOIN FETCH t.seat
                JOIN FETCH b.user u
                WHERE b.status = 'PAID'
                AND (
                    CAST(b.bookingId AS string) LIKE %:keyword%
                    OR LOWER(u.profile.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                ORDER BY b.bookedAt DESC
            """)
    List<Booking> searchPaidBookings(@Param("keyword") String keyword);

    long countByStatus(BookingStatus status);

    List<Booking> findByStatusInOrderByBookedAtDesc(Collection<BookingStatus> statuses);


    // Query thống kê
    // Doanh thu từng tháng trong năm hiện tại
    @Query("""
                select month(b.bookedAt) as monthBooked, sum(b.totalAmount) as revenue from Booking b where b.status = 'CONFIRMED' and year(b.bookedAt) = year(now()) group by month(b.bookedAt) order by month(b.bookedAt) asc
            """)
    List<Object[]> getMonthRevenue();

    // Top 5 phim có donanh thu cao nhất
    @Query("""
            select m.movieTitle as movie_title, sum(b.totalAmount) as revenue, count(distinct b.bookingId)
                        from Booking b join Ticket t on t.booking.bookingId = b.bookingId
                             join ShowTime s on s.showId = t.showTime.showId
                                         join Movie m on m.movieId = s.movie.movieId
                                         where b.status = 'CONFIRMED' group by m.movieId, m.movieTitle
                                                     order by revenue desc
                                                                 limit 5 offset 0
            """)
    List<Object[]> getTop5Movie();

    // Tính tổng doanh thu
    @Query("""
        select coalesce(sum(b.totalAmount)) from Booking b where b.status = 'CONFIRMED'
        """)
    BigDecimal getTotalRevenue();

    long countBookingByStatus(BookingStatus status);




}
