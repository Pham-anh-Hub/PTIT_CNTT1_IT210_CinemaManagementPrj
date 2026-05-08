package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter @Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<Ticket> tickets; // Phải có trường này để JOIN FETCH hoạt động
}