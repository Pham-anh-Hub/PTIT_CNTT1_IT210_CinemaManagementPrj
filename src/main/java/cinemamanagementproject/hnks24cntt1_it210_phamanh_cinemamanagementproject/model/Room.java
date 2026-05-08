package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "rooms")
@Getter @Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name")
    private String roomName;
    @Column(name = "total_seat")
    private Integer totalSeat;

    @OneToMany(mappedBy = "room")
    private List<Seat> seats;

    @OneToMany(mappedBy = "room")
    private List<ShowTime> showTimes;
}