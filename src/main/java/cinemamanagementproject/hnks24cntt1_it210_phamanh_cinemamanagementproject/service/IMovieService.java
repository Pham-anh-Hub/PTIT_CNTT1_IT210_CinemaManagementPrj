package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Movie;

import java.util.List;

public interface IMovieService {

    List<MovieResponseDTO> getAllMovies();
    Movie getMovieById(Long id);
    List<MovieResponseDTO> searchMovies(String keyword, MovieStatus status);
    MovieResponseDTO getMovieDTOById(Long id);
    MovieRequestDTO getMovieForEdit(Long id); // load về form edit
    void saveMovie(MovieRequestDTO dto);              // thêm mới
    void updateMovie(MovieRequestDTO dto);            // cập nhật
    void deleteMovie(Long id);

    // Stats cho dashboard cards
    long countAll();
    long countByStatus(MovieStatus status);
}