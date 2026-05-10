package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.*;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
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

    public Booking getBookingById(Long id){
        return bookingRepository.getReferenceById(id);
    }

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

    public List<Booking> getBookingByUserId(Long userId) {
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
        boolean hasConflict = ticketRepository.existsAnyActiveTicket(
                showId,
                seatIds,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (hasConflict) {
            // Nếu muốn báo chính xác ghế nào bị trùng, bạn có thể dùng findActiveByShowId
            // rồi so sánh, nhưng thông báo chung này đã đủ để chặn lỗi rồi.
            throw new RuntimeException("Một trong những ghế bạn chọn vừa có người giữ. Vui lòng chọn lại!");
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

        // THÊM TRY-CATCH: Để bắt lỗi Duplicate Entry nếu 2 người nhấn cùng lúc
        try {
            ticketRepository.saveAll(tickets);
        } catch (Exception e) {
            // Bắt các lỗi vi phạm ràng buộc UNIQUE của Database
            throw new RuntimeException("Ghế đã được đặt bởi người khác. Vui lòng thử lại!");
        }

        return booking;
    }

    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));
        if (booking.getStatus() == BookingStatus.PAID) {
            throw new RuntimeException("Đơn hàng đã thanh toán rồi");
        } else if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Đơn đặt vé không ở trạng thái chờ thanh toán");
        }

        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
    }

    // Chủ động hủy vé phía khách hàng
    @Transactional
    public void cancelByUser(Long bookingId, Long userId) {
        // Tìm đơn đặt vé
        Booking booking = bookingRepository.findByIdWithTickets(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        // Kiểm tra đơn có thuộc về khách hangf này không
        if (!booking.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn này");
        }

        // 1. Nếu đã hủy rồi thì không làm gì cả
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCELLED_PENDING) {
            throw new RuntimeException("Yêu cầu hủy đã được thực hiện trước đó");
        }

        // 2. Xử lý theo từng trạng thái hiện tại của đơn
        if (booking.getStatus() == BookingStatus.PENDING) {
            // HƯỚNG 1: Đơn chưa thanh toán -> Hủy trực tiếp ngay lập tức
            booking.setStatus(BookingStatus.CANCELLED);
            // (Tùy chọn) Xóa tickets hoặc giải phóng ghế ở đây nếu cần

        } else if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PAID) {
            // HƯỚNG 2: Đơn đã thanh toán (CONFIRMED hoặc PAID)

            // Kiểm tra thời gian (chỉ cho phép yêu cầu hủy trước 24h)
            LocalDateTime showTime = booking.getTickets().get(0).getShowTime().getStartAt();
            if (LocalDateTime.now().isAfter(showTime.minusHours(24))) {
                throw new RuntimeException("Không thể hủy vé — Phải thực hiện trước giờ chiếu 24 tiếng");
            }

            // Chuyển sang trạng thái "Chờ xác nhận hủy" thay vì hủy thẳng
            // Để Staff kiểm tra rồi mới chuyển sang CANCELLED sau khi hoàn tiền
            booking.setStatus(BookingStatus.CANCELLED_PENDING);

        } else {
            throw new RuntimeException("Trạng thái đơn hàng không hỗ trợ hủy");
        }
        bookingRepository.save(booking);
    }

    // Chạy mỗi 5 phút — tự động hủy các đơn PENDING đã hết hạn
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void autoExpirePendingBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả đơn PENDING
        List<Booking> pendingBookings = bookingRepository.findPendingWithShowTime(BookingStatus.PENDING);

        List<Booking> toCancel = pendingBookings.stream()
                .filter(b -> !b.getTickets().isEmpty())
                .filter(b -> {
                    LocalDateTime showStart = b.getTickets().getFirst().getShowTime().getStartAt();
                    return now.isAfter(showStart); // suất chiếu đã qua
                })
                .toList();

        toCancel.forEach(b -> b.setStatus(BookingStatus.CANCELLED));
        bookingRepository.saveAll(toCancel);

        if (!toCancel.isEmpty()) {
            System.out.println("[AutoExpire] Đã hủy " + toCancel.size() + " đơn hết hạn");
        }
    }

    @Transactional
    public void confirmByStaff(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithTickets(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() != BookingStatus.PAID) {
            throw new RuntimeException("Chỉ xác nhận được đơn đã thanh toán (PAID)");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void approveCancelRequest(Long bookingId) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng mã #" + bookingId));

        // 2. Kiểm tra trạng thái hợp lệ (Chỉ cho phép duyệt nếu đang là CANCELLED_PENDING)
        if (booking.getStatus() != BookingStatus.CANCELLED_PENDING) {
            throw new RuntimeException("Đơn hàng này không có yêu cầu hủy hợp lệ!");
        }

        // 3. Đổi trạng thái đơn hàng thành ĐÃ HỦY
        booking.setStatus(BookingStatus.CANCELLED);

        // 4. XỬ LÝ NHẢ GHẾ (Rất quan trọng)
        // Tùy thuộc vào thiết kế hệ thống của bạn, chọn 1 trong 2 cách sau:

        /* CÁCH 1: Nếu Entity Ticket của bạn CÓ trường trạng thái (ví dụ TicketStatus)
        booking.getTickets().forEach(ticket -> {
            ticket.setStatus(TicketStatus.CANCELLED);
        });
        */

        // CÁCH 2: Nếu Entity Ticket KHÔNG CÓ trạng thái, bạn phải XÓA vé đi để ghế đó hiện trống lại
        ticketRepository.deleteAll(booking.getTickets());
        booking.getTickets().clear();


        // 6. Lưu thay đổi
        bookingRepository.save(booking);
    }
}