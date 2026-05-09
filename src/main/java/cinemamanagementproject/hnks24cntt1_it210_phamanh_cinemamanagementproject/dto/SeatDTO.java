package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.SeatLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class SeatDTO {
    private Long seatId;
    private String seatName; // A1, A2...
    private SeatLevel seatLevel;
    private BigDecimal price;
    private boolean booked;
    private boolean selecting;
}