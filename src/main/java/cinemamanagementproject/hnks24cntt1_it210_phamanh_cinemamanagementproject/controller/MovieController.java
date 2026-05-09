package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Movie;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.ShowTime;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.IMovieService;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.ShowTimeService;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.TicketService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cinema/movie")
@RequiredArgsConstructor
public class MovieController {

    private final IMovieService movieService; // Giả định bạn đã có Service
    private final ShowTimeService showTimeService;
    private final TicketService ticketService;
    private final BookingService bookingService;

    // Khởi tạo session nếu chưa có
    public List<Long> initSelectedSeats() {
        return new ArrayList<>();
    }

    @GetMapping("/detail/{id}")
    public String getMovieDetail(@PathVariable("id") Long id,
                                 @RequestParam(required = false) Long showId,
                                 HttpSession session,
                                 Model model) {

        // 1. Lấy thông tin phim (vẫn để ở đây vì thuộc MovieController)
        model.addAttribute("movie", movieService.getMovieById(id));

        if (showId != null) {
            session.setAttribute("selectedShowId", showId);

            // Đọc selectedSeatIds từ session để hiển thị, KHÔNG toggle
            List<Long> selectingIds = (List<Long>) session.getAttribute("selectedSeatIds");
            if (selectingIds == null) selectingIds = new ArrayList<>();

            bookingService.populateBookingModel(model, showId, selectingIds);
        }

        return "detail_movie";
    }

    private BigDecimal calculateTotal(List<SeatDTO> seatMap, List<Long> selectedIds) {
        return seatMap.stream()
                .filter(s -> selectedIds.contains(s.getSeatId()))
                .map(SeatDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}