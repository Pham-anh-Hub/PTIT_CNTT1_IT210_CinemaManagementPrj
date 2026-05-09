package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.*;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowTimeRepository showTimeRepository;
    private final UserRepository userRepository;
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

    public List<Booking> getBookingByUserId(Long userId){
        return bookingRepository.findHistoryByUserId(userId);
    }

    @Transactional
    public Booking createPendingBooking(Long userId, Long showId, List<Long> seatIds) {

        // 1. Lấy ShowTime (đã có basePrice, room, movie)
        ShowTime showTime = showTimeRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));

        // 2. Lấy User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // 3. Lấy danh sách Seat theo seatIds được chọn
        List<Seat> seats = seatRepository.findAllById(seatIds);

        // Kiểm tra đủ số ghế (tránh trường hợp seatId không hợp lệ)
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Một số ghế không tồn tại!");
        }

        // 4. Kiểm tra từng ghế có bị đặt trong suất chiếu này chưa
        for (Seat seat : seats) {
            boolean alreadyBooked = ticketRepository
                    .existsByShowTime_ShowIdAndSeat_SeatId(showId, seat.getSeatId());
            if (alreadyBooked) {
                throw new RuntimeException(
                        "Ghế " + seat.getSeatName() + " vừa được người khác đặt. Vui lòng chọn ghế khác!"
                );
            }
        }

        // 5. Tính tổng tiền:
        //    ticketPrice = showTime.basePrice * seat.seatModifier
        //    Ví dụ: basePrice = 100.000đ, seatModifier = 1.5 (VIP) => 150.000đ
        BigDecimal basePrice = showTime.getBasePrice();

        BigDecimal totalAmount = seats.stream()
                .map(seat -> basePrice.multiply(seat.getSeatModifier()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Tạo và lưu Booking với status PENDING
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookedAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking); // Lưu trước để có bookingId

        // 7. Tạo Ticket cho từng ghế
        List<Ticket> tickets = seats.stream().map(seat -> {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowTime(showTime);
            ticket.setSeat(seat);
            ticket.setTicketPrice(basePrice.multiply(seat.getSeatModifier())); // đúng field name
            return ticket;
        }).collect(Collectors.toList());

        ticketRepository.saveAll(tickets);

        return booking;
    }

    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Đơn đặt vé không ở trạng thái chờ thanh toán");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }
}