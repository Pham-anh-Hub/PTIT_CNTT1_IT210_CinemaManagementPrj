package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// dto/ShowTimeDTO.java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ShowTimeDTO {
    private Long showId;
    private String movieTitle;
    private Long movieId;
    private Long roomId;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endedAt;
    private BigDecimal basePrice;
    private String status; // "UPCOMING" | "NOW_SHOWING" | "ENDED"
}