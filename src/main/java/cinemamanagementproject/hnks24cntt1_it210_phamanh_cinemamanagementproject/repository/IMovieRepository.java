package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Movie;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> getMovieByStatus(MovieStatus status);

    // Tìm kiếm theo tên (không phân biệt hoa thường)
    @Query("SELECT m FROM Movie m WHERE " +
            "(:keyword IS NULL OR LOWER(m.movieTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR m.status = :status)")
    List<Movie> searchMovies(@Param("keyword") String keyword,
                             @Param("status") MovieStatus status);

    // Đếm theo trạng thái — dùng cho stats card
    long countByStatus(MovieStatus status);
}
