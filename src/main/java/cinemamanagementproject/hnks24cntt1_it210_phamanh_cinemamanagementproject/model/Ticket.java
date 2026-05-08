package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(
    name = "tickets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"})
)
@Getter @Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private ShowTime showTime;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private BigDecimal ticketPrice;
}