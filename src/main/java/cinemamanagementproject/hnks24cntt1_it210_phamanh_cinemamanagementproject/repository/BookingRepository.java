package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}
