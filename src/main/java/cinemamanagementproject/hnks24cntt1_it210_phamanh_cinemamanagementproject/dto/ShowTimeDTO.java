package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// dto/ShowTimeDTO.java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ShowTimeDTO {
    private Long movieId;
    private Long roomId;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    private BigDecimal basePrice;
}