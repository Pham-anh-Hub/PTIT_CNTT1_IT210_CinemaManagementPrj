package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.GenreRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.IMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cinema/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final IMovieService movieService;
    private final GenreRepository genreRepository;

    /** Danh sách + tìm kiếm */
    @GetMapping
    public String listMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MovieStatus status,
            Model model
    ) {
        List<MovieResponseDTO> movies = (keyword != null || status != null)
                ? movieService.searchMovies(keyword, status)
                : movieService.getAllMovies();

        model.addAttribute("movies",movies);
        model.addAttribute("keyword", keyword);
        model.addAttribute("filterStatus", status);
        model.addAttribute("statuses", MovieStatus.values());

        // Stats cards
        model.addAttribute("totalMovies", movieService.countAll());
        model.addAttribute("nowShowing", movieService.countByStatus(MovieStatus.NOW_SHOWING));
        model.addAttribute("upcoming", movieService.countByStatus(MovieStatus.UPCOMING));

        return "admin/admin-movie-list";
    }

    /** Form thêm mới */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("movieForm", new MovieRequestDTO());
        model.addAttribute("allGenres",   genreRepository.findAllByOrderByGenresNameAsc());
        model.addAttribute("statuses",    MovieStatus.values());
        model.addAttribute("isEdit",      false);
        return "auth-form/movie-form";
    }

    /** Form chỉnh sửa */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("movieForm", movieService.getMovieForEdit(id));
        model.addAttribute("allGenres",   genreRepository.findAllByOrderByGenresNameAsc());
        model.addAttribute("statuses",    MovieStatus.values());
        model.addAttribute("isEdit",      true);
        return "auth-form/movie-form";
    }

    /** Lưu (thêm mới hoặc cập nhật) */
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("movieForm") MovieRequestDTO dto,
            BindingResult bindingResult,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            Model model,
            RedirectAttributes ra
    ) {
        if (posterFile != null && !posterFile.isEmpty()) dto.setPosterFile(posterFile);

        if (bindingResult.hasErrors()) {
            model.addAttribute("allGenres", genreRepository.findAllByOrderByGenresNameAsc());
            model.addAttribute("statuses",  MovieStatus.values());
            model.addAttribute("isEdit",    dto.getMovieId() != null);
            return "auth-form/movie-form";
        }

        if (dto.getMovieId() == null) {
            movieService.saveMovie(dto);
            ra.addFlashAttribute("successMsg", "Thêm phim \"" + dto.getMovieTitle() + "\" thành công!");
        } else {
            movieService.updateMovie(dto);
            ra.addFlashAttribute("successMsg", "Cập nhật phim \"" + dto.getMovieTitle() + "\" thành công!");
        }

        return "redirect:/cinema/admin/movies";
    }

    /** Xóa phim */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            movieService.deleteMovie(id);
            ra.addFlashAttribute("successMsg", " Đã xóa phim thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không thể xóa phim (vui lòng kiểm tra lại)");
        }
        return "redirect:/cinema/admin/movies";
    }
}