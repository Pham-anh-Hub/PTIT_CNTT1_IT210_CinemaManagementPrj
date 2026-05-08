package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.SeatLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(name = "seats")
@Getter @Setter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "seat_name")
    private String seatName;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    private SeatLevel seatLevel;

    private BigDecimal seatModifier;

    @OneToMany(mappedBy = "seat")
    private List<Ticket> tickets;
}