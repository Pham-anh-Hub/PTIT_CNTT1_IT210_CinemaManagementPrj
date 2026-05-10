package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.BookingRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.IMovieRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.ProfileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final IMovieRepository movieRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final ProfileServiceImpl profileService;
    private final BookingRepository bookingRepository;

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("movieNowShowing", movieRepository.getMovieByStatus(MovieStatus.NOW_SHOWING));
        model.addAttribute("movieUpcoming", movieRepository.getMovieByStatus(MovieStatus.UPCOMING));
        model.addAttribute("activePage", "home");
        return "home_page";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/admin-movie-list";
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        return "staff-booking-list";
    }

    @GetMapping("/user/booking-list")
    public String viewBookingHistory(
            Authentication authentication,
            @RequestParam(required = false) String movieTitle,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Long userId = profileService.getCurrentUserId(authentication);

        // Parse params
        BookingStatus bookingStatus = (status != null && !status.isBlank()) ? BookingStatus.valueOf(status) : null;

        LocalDateTime from = (fromDate != null && !fromDate.isBlank()) ? LocalDate.parse(fromDate).atStartOfDay() : null;

        LocalDateTime to = (toDate != null && !toDate.isBlank()) ? LocalDate.parse(toDate).atTime(23, 59, 59) : null;

        String title = (movieTitle != null && !movieTitle.isBlank()) ? movieTitle : null;

        model.addAttribute("activePage", "booking-list");
        model.addAttribute("bookings", bookingRepository.findHistoryWithFilters(userId, bookingStatus, title, from, to));

        // Giữ lại giá trị filter để hiển thị lại trên form
        model.addAttribute("filterMovie", movieTitle);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterFrom", fromDate);
        model.addAttribute("filterTo", toDate);

        return "booking-history";
    }

    @GetMapping("/user/booking-detail/{id}")
    public String viewBookingDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findByIdWithTickets(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        //
        Long userId = profileService.getCurrentUserId(authentication);
        if (!booking.getUser().getUserId().equals(userId)) {
            return "redirect:/user/booking-list";
        }

        model.addAttribute("booking", booking);
        return "booking-detail";
    }

    // nghiệp vụ xử lý hủy vé
    @PostMapping("/user/booking/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = profileService.getCurrentUserId(authentication);

        try {
            bookingService.cancelByUser(id, userId);
            Booking booking = bookingService.getBookingById(id);
            if (booking.getStatus() == BookingStatus.CANCELLED_PENDING){
                redirectAttributes.addFlashAttribute("successMsg", "Yêu cầu hủy vé đã được gửi đi, vui lòng chờ xác nhận hoàn tiền!");
            }else {
                redirectAttributes.addFlashAttribute("successMsg", "Đã hủy đơn hàng thành công!");
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/user/booking-list";
    }
}
