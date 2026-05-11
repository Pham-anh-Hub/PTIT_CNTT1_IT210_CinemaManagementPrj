package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Genre;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Movie;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.GenreRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.IMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements IMovieService {

    private final IMovieRepository movieRepository;
    private final GenreRepository genreRepository;

    private static final String UPLOAD_DIR =
        "D:\\JavaWeb App\\HN-KS24-CNTT1_IT210_PhamAnh_CinemaManagementProject" +
        "\\src\\main\\resources\\static\\images\\";

    @Override
    public List<MovieResponseDTO> getAllMovies() {
        return movieRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Override
    public List<MovieResponseDTO> searchMovies(String keyword, MovieStatus status) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        return movieRepository.searchMovies(kw, status)
                              .stream().map(this::toResponseDTO).toList();
    }

    @Override
    public MovieResponseDTO getMovieDTOById(Long id) {
        return toResponseDTO(findMovie(id));
    }

    @Override
    public Movie getMovieById(Long id) {
        return movieRepository.getReferenceById(id);
    }

    @Override
    public MovieRequestDTO getMovieForEdit(Long id) {
        Movie movie = findMovie(id);
        MovieRequestDTO dto = new MovieRequestDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDurations(movie.getDurations());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setDescription(movie.getDescription());
        dto.setDirector(movie.getDirector());
        dto.setCasts(movie.getCasts());
        dto.setRatingAge(movie.getRatingAge());
        dto.setReleasedDate(movie.getReleasedDate());
        dto.setStatus(movie.getStatus());
        dto.setGenreIds(movie.getGenres().stream().map(Genre::getGenresId).toList());
        return dto;
    }

    @Override
    @Transactional
    public void saveMovie(MovieRequestDTO dto) {
        Movie movie = new Movie();
        mapDtoToEntity(dto, movie);
        movie.setCreatedAt(LocalDateTime.now());
        if (movie.getReleasedDate().isBefore(LocalDate.now())){
            movie.setStatus(MovieStatus.NOW_SHOWING);
        }else if (movie.getReleasedDate().isAfter(LocalDate.now())){
            movie.setStatus(MovieStatus.UPCOMING);
        }
        movieRepository.save(movie);
    }

    @Override
    @Transactional
    public void updateMovie(MovieRequestDTO dto) {
        Movie movie = findMovie(dto.getMovieId());
        mapDtoToEntity(dto, movie);
        if (movie.getReleasedDate().isBefore(LocalDate.now())){
            movie.setStatus(MovieStatus.NOW_SHOWING);
        }else if (movie.getReleasedDate().isAfter(LocalDate.now())){
            movie.setStatus(MovieStatus.UPCOMING);
        }
        movieRepository.save(movie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        //  Tìm phim trong Database
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim để xóa!"));

        // Chuyển trạng thái sang Ngừng chiếu (Xóa mềm)
        movie.setStatus(MovieStatus.ENDED);

        // Lưu lại thay đổi
        try {
            movieRepository.save(movie);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi cập nhật trạng thái phim: " + e.getMessage());
        }
    }

    @Override public long countAll()                     {
        return movieRepository.count(); }
    @Override public long countByStatus(MovieStatus s)   {
        return movieRepository.countByStatus(s); }

    // ── Helpers ───────────────────────────────────────────────────

    private void mapDtoToEntity(MovieRequestDTO dto, Movie movie) {
        movie.setMovieTitle(dto.getMovieTitle());
        movie.setDurations(dto.getDurations());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setDescription(dto.getDescription());
        movie.setDirector(dto.getDirector());
        movie.setCasts(dto.getCasts());
        movie.setRatingAge(dto.getRatingAge());
        movie.setReleasedDate(dto.getReleasedDate());
        movie.setStatus(dto.getStatus());

        // Poster: ưu tiên file upload → URL → giữ ảnh cũ
        if (dto.getPosterFile() != null && !dto.getPosterFile().isEmpty()) {
            movie.setPosterUrl("/images/movies/" + savePosterFile(dto.getPosterFile(), dto.getMovieTitle()));
        } else if (dto.getPosterUrl() != null && !dto.getPosterUrl().isBlank()) {
            movie.setPosterUrl(dto.getPosterUrl());
        }

        // Genres
        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
            movie.setGenres(genres);
        } else {
            movie.setGenres(new ArrayList<>());
        }
    }

    private String savePosterFile(MultipartFile file, String movieTitle) {
        try {
            Path dir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String ext = Optional.ofNullable(file.getOriginalFilename())
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf(".")))
                    .orElse(".jpg");

            String filename = "movie_" + System.currentTimeMillis() + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu poster phim", e);
        }
    }

    private Movie findMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim id=" + id));
    }

    private MovieResponseDTO toResponseDTO(Movie m) {
        return MovieResponseDTO.builder()
                .movieId(m.getMovieId())
                .movieTitle(m.getMovieTitle())
                .durations(m.getDurations())
                .posterUrl(m.getPosterUrl())
                .trailerUrl(m.getTrailerUrl())
                .description(m.getDescription())
                .director(m.getDirector())
                .casts(m.getCasts())
                .ratingAge(m.getRatingAge())
                .releasedDate(m.getReleasedDate())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .genreNames(m.getGenres() != null
                        ? m.getGenres().stream().map(Genre::getGenresName).toList()
                        : List.of())
                .genreIds(m.getGenres() != null
                        ? m.getGenres().stream().map(Genre::getGenresId).toList()
                        : List.of())
                .build();
    }
}