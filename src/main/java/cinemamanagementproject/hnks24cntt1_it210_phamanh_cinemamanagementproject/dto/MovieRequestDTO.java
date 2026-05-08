package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MovieRequestDTO {

    private Long movieId; // null = thêm mới, có giá trị = cập nhật

    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 200)
    private String movieTitle;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng tối thiểu 1 phút")
    @Max(value = 300, message = "Thời lượng tối đa 300 phút")
    private Integer durations;

    // Poster: nhập URL hoặc upload file — ưu tiên file nếu có cả 2
    private String posterUrl;
    private MultipartFile posterFile;

    private String trailerUrl;

    @Size(max = 2000)
    private String description;

    @Size(max = 100)
    private String director;

    @Size(max = 500)
    private String casts;

    private String ratingAge; // P, K, T13, T16, T18

    @NotNull(message = "Ngày phát hành không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releasedDate;

    @NotNull(message = "Trạng thái không được để trống")
    private MovieStatus status;

    private List<Long> genreIds = new ArrayList<>();
}