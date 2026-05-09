package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.ProfileServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cinema/booking")

@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ProfileServiceImpl profileService;

    @GetMapping("/select-seat")
    public String selectSeat(@RequestParam Long movieId,
                             @RequestParam Long showId,       // ← có nhận
                             @RequestParam(required = false) Long toggleSeat,
                             HttpSession session) {

        session.setAttribute("selectedShowId", showId); //
        bookingService.handleSeatToggle(session, toggleSeat); // ← chỉ lưu ghế, không lưu showId
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

    @PostMapping("/payment")
    public String createPendingBooking(
            HttpSession session,
            Authentication authentication,  // lấy userId từ đây
            RedirectAttributes redirectAttributes
    ) {
        // 1. Lấy dữ liệu từ session
        List<Long> seatIds = (List<Long>) session.getAttribute("selectedSeatIds");
        Long showId = (Long) session.getAttribute("selectedShowId");

        // 2. Tạo Booking PENDING vào DB
        try {
            Long userId = profileService.getCurrentUserId(authentication);

            Booking pendingBooking = bookingService.createPendingBooking(userId, showId, seatIds);

            // 3. Xoá session ghế đã chọn (đã lưu DB rồi)
            session.removeAttribute("selectedSeatIds");
            session.removeAttribute("selectedShowId");

            // 4. Chuyển tới trang thanh toán kèm bookingId
            return "redirect:/cinema/payment/" + pendingBooking.getBookingId();

        } catch (RuntimeException e) {
            // Ghế bị người khác đặt mất → quay lại báo lỗi
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cinema/booking/confirm";
        }
    }
}