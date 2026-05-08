package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cinema/booking")
@SessionAttributes("selectedSeatIds")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/select-seat")
    public String selectSeat(@RequestParam Long movieId,
                             @RequestParam Long showId,
                             @RequestParam(required = false) Long toggleSeat,
                             HttpSession session) {

        // 1. Giao cho Service xử lý việc thêm/bớt ghế vào Session
        bookingService.handleSeatToggle(session, toggleSeat);

        // 2. Chuyển hướng người dùng quay lại trang chi tiết kèm showId
        // Spring Security sẽ tự động chặn nếu người dùng chưa đăng nhập khi truy cập URL này
        return "redirect:/cinema/movie/detail/" + movieId + "?showId=" + showId;
    }

    @GetMapping("/confirm")
    public String showConfirmPage(HttpSession session, Model model) {
        // 1. Lấy danh sách ID ghế từ Session
        List<Long> selectingIds = (List<Long>) session.getAttribute("selectedSeatIds");

        if (selectingIds == null || selectingIds.isEmpty()) {
            // Nếu không có ghế nào được chọn, quay lại trang chủ hoặc báo lỗi
            return "redirect:/home";
        }

        // 2. Gọi Service để lấy thông tin chi tiết (Tên ghế, Suất chiếu, Tổng tiền)
        // Bạn có thể dùng chung logic populateBookingModel đã viết ở BookingService
        bookingService.populateConfirmModel(model, session);

        return "booking-confirm"; // Trả về file
    }
}