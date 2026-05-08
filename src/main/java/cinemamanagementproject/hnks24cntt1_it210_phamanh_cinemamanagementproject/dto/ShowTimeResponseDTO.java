package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// dto/ShowTimeResponseDTO.java
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ShowTimeResponseDTO {
    private Long showId;
    private String movieTitle;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endedAt;
    private BigDecimal basePrice;
    private String status;          // "UPCOMING" | "NOW_SHOWING" | "ENDED"
}