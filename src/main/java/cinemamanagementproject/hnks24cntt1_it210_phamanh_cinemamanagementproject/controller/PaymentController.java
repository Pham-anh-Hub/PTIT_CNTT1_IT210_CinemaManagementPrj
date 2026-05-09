package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.BookingRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cinema/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    // Hiển thị trang thanh toán với thông tin booking
    @GetMapping("/{bookingId}")
    public String showPaymentPage(
            @PathVariable Long bookingId,
            Model model
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        // Bảo vệ: chỉ cho xem nếu còn PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            return "redirect:/user/booking-list";
        }

        model.addAttribute("booking", booking);
        return "payment"; // tên file html của bạn
    }

    // Xử lý khi user bấm xác nhận thanh toán
    @PostMapping("/{bookingId}/confirm")
    public String confirmPayment(
            @PathVariable Long bookingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.confirmPayment(bookingId);
            redirectAttributes.addFlashAttribute("successMsg", "Đặt vé thành công!");
            return "redirect:/user/booking-detail/" + bookingId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cinema/payment/" + bookingId;
        }
    }
}