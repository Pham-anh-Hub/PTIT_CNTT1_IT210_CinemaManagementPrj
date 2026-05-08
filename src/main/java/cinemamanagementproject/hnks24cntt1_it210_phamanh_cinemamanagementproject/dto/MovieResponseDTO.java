package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class MovieResponseDTO {

    private Long  movieId;
    private String movieTitle;
    private Integer durations;
    private String posterUrl;
    private String trailerUrl;
    private String description;
    private String director;
    private String casts;
    private String ratingAge;
    private LocalDate releasedDate;
    private MovieStatus status;
    private LocalDateTime createdAt;

    // Dùng để hiển thị tag thể loại
    private List<String> genreNames;
    // Dùng để pre-check checkbox khi edit
    private List<Long>   genreIds;
}