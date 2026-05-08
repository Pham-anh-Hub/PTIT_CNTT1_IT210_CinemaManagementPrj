package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.*;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.BookingRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.SeatRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.ShowTimeRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.TicketRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

     private final BookingRepository bookingRepository;
     private final TicketRepository ticketRepository;
     private final ShowTimeRepository showTimeRepository;
     private final SeatRepository seatRepository;
    private final ShowTimeService showTimeService;

    // 1. Xử lý logic chọn/hủy ghế và trả về list ID mới
    public List<Long> handleSeatToggle(HttpSession session, Long toggleSeat) {
        List<Long> selectingIds = (List<Long>) session.getAttribute("selectedSeatIds");
        if (selectingIds == null) {
            selectingIds = new ArrayList<>();
        }

        if (toggleSeat != null) {
            if (selectingIds.contains(toggleSeat)) {
                selectingIds.remove(toggleSeat);
            } else {
                selectingIds.add(toggleSeat);
            }
        }

        session.setAttribute("selectedSeatIds", selectingIds);
        return selectingIds;
    }

    // 2. Gom dữ liệu cần thiết cho View vào một Map hoặc một DTO riêng
    public void populateBookingModel(Model model, Long showId, List<Long> selectingIds) {
        // 1. Lấy thông tin suất chiếu và phòng
        ShowTime showTime = showTimeService.findById(showId);
        int totalSeat = showTime.getRoom().getTotalSeat();

        // 2. Lấy danh sách SeatDTO (hàm này đã truy vấn trực tiếp từ SeatRepository)
        // Phải đảm bảo size của seatMap này bằng với totalSeat của Room
        List<SeatDTO> seatMap = showTimeService.getSeatMapForShow(showId, selectingIds);

        // 3. Tính toán số cột để chia hàng (Ví dụ: 10 ghế/hàng)
        int columns = 10;
        // 4. Đẩy tất cả vào Model
        model.addAttribute("selectedShow", showTime);
        model.addAttribute("seatMap", seatMap); // Chứa đúng số lượng ghế của phòng
        model.addAttribute("totalSeat", totalSeat);
        model.addAttribute("columns", columns);

        // Tính tổng tiền dựa trên các ghế đang Selecting
        model.addAttribute("totalPrice", calculateTotal(seatMap));

        // Lấy danh sách tên ghế đang chọn để hiện thị (Ví dụ: A1, A2)
        List<String> selectedNames = seatMap.stream()
                .filter(SeatDTO::isSelecting)
                .map(SeatDTO::getSeatName)
                .toList();
        model.addAttribute("selectedSeatNames", selectedNames);
    }

    public void populateConfirmModel(Model model, HttpSession session) {
        List<Long> selectingIds = (List<Long>) session.getAttribute("selectedSeatIds");
        Long showId = (Long) session.getAttribute("selectedShowId");

        // Kiểm tra nếu showId hoặc danh sách ghế bị trống thì redirect hoặc báo lỗi nhẹ nhàng
        if (showId == null || selectingIds == null || selectingIds.isEmpty()) {
            throw new RuntimeException("Phiên làm việc đã hết hạn hoặc bạn chưa chọn ghế!");
        }

        List<SeatDTO> seatMap = showTimeService.getSeatMapForShow(showId, selectingIds);

        model.addAttribute("selectedSeats", seatMap.stream().filter(SeatDTO::isSelecting).toList());
        model.addAttribute("totalPrice", calculateTotal(seatMap));
        model.addAttribute("showTime", showTimeService.findById(showId));
    }
    private BigDecimal calculateTotal(List<SeatDTO> seatMap) {
        return seatMap.stream()
                .filter(SeatDTO::isSelecting)
                .map(SeatDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}