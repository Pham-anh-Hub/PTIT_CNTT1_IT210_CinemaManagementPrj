package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;


import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.Booking;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.BookingRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
public class StaffBookingController {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;


    // Lấy ra danh sách đơn đặt đã thanh toán
    @GetMapping
    public String listPaidBooking(
            @RequestParam(name = "searchBooking", defaultValue = "") String keyword,
            Model model
    ){
        List<Booking> bookings;
        if(keyword != null && !keyword.isBlank()){
            // Tìm theo mã đơn (số) hoặc tên khách hàng
            bookings = bookingRepository.searchPaidBookings(keyword.trim());
        } else {
            List<BookingStatus> statuses = Arrays.asList(BookingStatus.PAID, BookingStatus.CANCELLED_PENDING);
            bookings = bookingRepository.findByStatusInOrderByBookedAtDesc(statuses);
        }

        // danh sách các yêu cầu hủy đang chờ
        long cancelBookings = bookingRepository.countBookingByStatus(BookingStatus.CANCELLED_PENDING);
        model.addAttribute("bookings", bookings);
        model.addAttribute("cancelCounts", cancelBookings);
        model.addAttribute("keyword", keyword);

        return "staff-booking-list";
    }

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmByStaff(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đã xác nhận đơn #" + id + " thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    // xác nhận đơn hủy và yêu cầu hoàn tiền
    @PostMapping("/{id}/approve-cancel")
    public String approveCancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Logic: Chuyển status từ CANCELLED_PENDING sang CANCELLED
            // và cộng lại tiền cho khách (nếu có ví) hoặc đánh dấu đã hoàn tiền.
            bookingService.approveCancelRequest(id);
            redirectAttributes.addFlashAttribute("successMsg", "Đã duyệt yêu cầu hủy đơn #" + id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }
}
