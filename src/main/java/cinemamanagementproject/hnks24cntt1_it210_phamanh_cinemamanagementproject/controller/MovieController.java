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
    @ModelAttribute("selectedSeatIds")
    public List<Long> initSelectedSeats() {
        return new ArrayList<>();
    }

    @GetMapping("/detail/{id}")
    public String getMovieDetail(@PathVariable("id") Long id,
                                 @RequestParam(required = false) Long showId,
                                 @RequestParam(required = false) Long toggleSeat,
                                 HttpSession session,
                                 Model model) {

        // 1. Lấy thông tin phim (vẫn để ở đây vì thuộc MovieController)
        model.addAttribute("movie", movieService.getMovieById(id));

        // 2. Xử lý nghiệp vụ chọn ghế qua BookingService
        List<Long> selectingIds = bookingService.handleSeatToggle(session, toggleSeat);

        // 3. Nếu đã chọn suất chiếu, nhờ BookingService đổ dữ liệu vào Model
        if (showId != null) {
            // QUAN TRỌNG: Lưu showId vào session để trang Confirm có thể lấy ra
            session.setAttribute("selectedShowId", showId);
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