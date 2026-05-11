package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Movie;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.ShowTime;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cinema/movie")
@RequiredArgsConstructor
public class MovieController {

    private final IMovieService movieService; // Giả định bạn đã có Service
    private final ProfileServiceImpl profileService;
    private final BookingService bookingService;

    // Khởi tạo session nếu chưa có
    public List<Long> initSelectedSeats() {
        return new ArrayList<>();
    }

    @GetMapping("/detail/{id}")
    public String getMovieDetail(@PathVariable("id") Long id,
                                 @RequestParam(required = false) Long showId,
                                 HttpSession session,
                                 Authentication authentication,
                                 Model model) {

        Movie movie = movieService.getMovieById(id);
        Long userId = profileService.getCurrentUserId(authentication);

        // Lọc chỉ các suất chiếu sắp tới
        List<ShowTime> upcomingShows = movie.getShowTimes().stream()
                .filter(st -> st.getStartAt().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(ShowTime::getStartAt))
                .toList();

        model.addAttribute("movie", movie);
        model.addAttribute("upcomingShows", upcomingShows); // dùng list mới này

        if (showId != null) {
            session.setAttribute("selectedShowId", showId);
            List<Long> selectingIds = (List<Long>) session.getAttribute("selectedSeatIds");
            if (selectingIds == null) selectingIds = new ArrayList<>();
            bookingService.populateBookingModel(model, showId, userId, selectingIds);
        }

        return "detail_movie";
    }

    private BigDecimal calculateTotal(List<SeatDTO> seatMap, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) return BigDecimal.ZERO;

        return seatMap.stream()
                .filter(dto -> selectedIds.contains(dto.getSeatId())) // Chỉ tính ghế của MÌNH
                .map(SeatDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}