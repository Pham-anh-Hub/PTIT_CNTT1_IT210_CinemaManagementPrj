package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "show_times")
@Getter @Setter
public class ShowTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Long showId;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_at")
    private LocalDateTime startAt;
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "showTime")
    private List<Ticket> tickets;
}