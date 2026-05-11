package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ShowTimeDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.ShowStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.*;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowTimeService {
    private final ShowTimeRepository showTimeRepository;
    private final IMovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;

    public ShowTime findById(Long id) {
        return showTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu!"));
    }


    public List<ShowTimeDTO> searchShowTimes(String keyword, Long roomId, String status) {
        LocalDateTime now = LocalDateTime.now();

        return showTimeRepository.findAllWithDetails().stream()
                .filter(s -> s.getStatus() != null && s.getStatus() != ShowStatus.DELETED)
                .filter(s -> {
                    boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                            s.getMovie().getMovieTitle().toLowerCase().contains(keyword.toLowerCase());

                    boolean matchRoom = (roomId == null) ||
                            s.getRoom().getRoomId().equals(roomId);

                    // Tính status động theo giờ
                    String computed = computeStatus(s, now);
                    boolean matchStatus = (status == null || status.isEmpty()) ||
                            computed.equalsIgnoreCase(status); // "NOW_SHOWING" khớp luôn

                    return matchKeyword && matchRoom && matchStatus;
                })
                .map(s -> ShowTimeDTO.builder()
                        .showId(s.getShowId())
                        .movieTitle(s.getMovie().getMovieTitle())
                        .roomName(s.getRoom().getRoomName())
                        .startAt(s.getStartAt())
                        .endedAt(s.getEndedAt())
                        .basePrice(s.getBasePrice())
                        .status(computeStatus(s, now))
                        .build())
                .collect(Collectors.toList());
    }

    // Helper tính trạng thái theo thời gian thực
    private String computeStatus(ShowTime s, LocalDateTime now) {
        if (now.isBefore(s.getStartAt())) return "UPCOMING";
        if (now.isAfter(s.getEndedAt())) return "ENDED";
        return "NOW_SHOWING";
    }

    // Các phương thức đếm số lượng cho Dashboard
    public long getShowingNow() {
        return showTimeRepository.findNowShowing(LocalDateTime.now()).size();
    }

    public long getUpcoming() {
        return showTimeRepository.findUpcoming(LocalDateTime.now()).size();
    }

    @Transactional
    public ShowTime createShowTime(ShowTimeDTO dto) {
        // 1. Lấy thông tin Movie để biết thời lượng (duration)
        Movie movie = movieRepository.findById(dto.getMovieId()).orElseThrow(() -> new RuntimeException("Không tìm thấy phim!"));

        // 2. Lấy thông tin Room
        Room room = roomRepository.findById(dto.getRoomId()).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu!"));

        // 3. Tính toán endedAt = startAt + duration (phút) + 15 phút dọn phòng (tùy chọn)
        LocalDateTime startAt = dto.getStartAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể tạo suất chiếu trong quá khứ. Vui lòng chọn thời gian hiện tại hoặc tương lai.");
        }

        // 3. Kiểm tra nếu suất chiếu trước ngày công chiếu
        if (startAt.isBefore(movie.getReleasedDate().atStartOfDay())) {
            String formattedReleaseDate = movie.getReleasedDate().format(formatter);
            throw new RuntimeException("Phim chỉ được chiếu từ ngày công chiếu trở đi: " + formattedReleaseDate);
        }


        LocalDateTime endedAt = startAt.plusMinutes(movie.getDurations()).plusMinutes(15);

        // 4. Kiểm tra xung đột lịch chiếu bằng Query bạn đã viết
        // Truyền -1L vì đây là tạo mới, không cần loại trừ ID nào
        List<ShowTime> conflicts = showTimeRepository.findConflicts(
                room.getRoomId(),
                startAt,
                endedAt,
                -1L
        );

        if (!conflicts.isEmpty()) {
            ShowTime firstConflict = conflicts.getFirst();
            throw new RuntimeException("Xung đột lịch chiếu! Phòng này đã có phim '"
                    + firstConflict.getMovie().getMovieTitle()
                    + "' chiếu đến " + firstConflict.getEndedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // 5. Map DTO sang Entity và lưu
        ShowTime showTime = new ShowTime();
        showTime.setMovie(movie);
        showTime.setRoom(room);
        showTime.setStartAt(startAt);
        showTime.setEndedAt(endedAt);
        showTime.setBasePrice(dto.getBasePrice());

        return showTimeRepository.save(showTime);
    }

    @Transactional
    public ShowTime updateShowTime(Long id, ShowTimeDTO dto) {
        // 1. Kiểm tra suất chiếu cũ có tồn tại không
        ShowTime existingShowTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu để cập nhật!"));

        // 2. Lấy thông tin Movie và Room mới (từ DTO)
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim!"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu!"));

        // 3. Tính toán lại thời gian kết thúc
        LocalDateTime startAt = dto.getStartAt();
        LocalDateTime endedAt = startAt.plusMinutes(movie.getDurations()).plusMinutes(15);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể tạo suất chiếu trong quá khứ. Vui lòng chọn thời gian hiện tại hoặc tương lai.");
        }

        // 3. Kiểm tra nếu suất chiếu trước ngày công chiếu
        if (startAt.isBefore(movie.getReleasedDate().atStartOfDay())) {
            String formattedReleaseDate = movie.getReleasedDate().format(formatter);
            throw new RuntimeException("Phim chỉ được chiếu từ ngày công chiếu trở đi: " + formattedReleaseDate);
        }

        // 4. KIỂM TRA XUNG ĐỘT (QUAN TRỌNG: Truyền chính ID hiện tại vào excludeId)
        // Việc truyền 'id' vào giúp SQL bỏ qua chính bản ghi này khi kiểm tra chồng chéo
        List<ShowTime> conflicts = showTimeRepository.findConflicts(
                room.getRoomId(),
                startAt,
                endedAt,
                id // <--- Thay -1L bằng id của suất chiếu đang sửa
        );

        if (!conflicts.isEmpty()) {
            ShowTime firstConflict = conflicts.getFirst();
            throw new RuntimeException("Xung đột! Phòng đã có phim '"
                    + firstConflict.getMovie().getMovieTitle()
                    + "' chiếu đến " + firstConflict.getEndedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // 5. Cập nhật dữ liệu trên đối tượng đã tìm thấy (không tạo mới new ShowTime())
        existingShowTime.setMovie(movie);
        existingShowTime.setRoom(room);
        existingShowTime.setStartAt(startAt);
        existingShowTime.setEndedAt(endedAt);
        existingShowTime.setBasePrice(dto.getBasePrice());

        // 6. Lưu lại (Dirty checking của @Transactional sẽ tự update, nhưng gọi save cho rõ ràng)
        return showTimeRepository.save(existingShowTime);
    }

    @Transactional
    public void deleteShowTime(Long id) {
        // 1. Tìm suất chiếu chưa bị xóa
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu!"));

        // 2. Kiểm tra nếu đã xóa  rồi
        if (showTime.getStatus() == ShowStatus.DELETED) {
            throw new RuntimeException("Suất chiếu này đã bị xóa trước đó!");
        }
        LocalDateTime now = LocalDateTime.now();

        // 2. Kiểm tra thời gian kết thúc
        // Nếu suất chiếu CHƯA kết thúc (đang chiếu hoặc sắp chiếu) thì mới chặn xóa khi có vé
        if (showTime.getEndedAt().isAfter(now)) {

            if (showTime.getTickets() != null && !showTime.getTickets().isEmpty()) {
                // Kiểm tra xem có vé nào ở trạng thái CONFIRMED (đã thanh toán/xác nhận) không
                boolean hasActiveTickets = showTime.getTickets().stream()
                        .anyMatch(t -> t.getBooking().getStatus().equals("CONFIRMED") ||
                                t.getBooking().getStatus().equals("PAID"));

                if (hasActiveTickets) {
                    throw new RuntimeException("Suất chiếu đang/sắp diễn ra và đã có khách đặt vé. " +
                            "Không thể xóa để đảm bảo quyền lợi khách hàng!");
                }
            }
        }

        // 3. Thực hiện XÓA MỀM (Dành cho cả trường hợp đã kết thúc HOẶC chưa kết thúc nhưng không có vé)
        try {
            showTime.setStatus(ShowStatus.DELETED);
            showTimeRepository.save(showTime);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi thực hiện xóa: " + e.getMessage());
        }
    }

    public List<SeatDTO> getSeatMapForShow(Long showId, Long currentUserId, List<Long> selectingIds) {
        ShowTime showTime = showTimeRepository.findById(showId).orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));

        List<Seat> seatsInDb = seatRepository.findByRoom_RoomId(showTime.getRoom().getRoomId());

        // Lấy Map Ticket theo SeatId để tra cứu nhanh (O(1))
        Map<Long, Ticket> activeTickets = ticketRepository
                .findActiveByShowId(showId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))
                .stream()
                .collect(Collectors.toMap(t -> t.getSeat().getSeatId(), t -> t, (t1, t2) -> t1));

        return seatsInDb.stream().map(seat -> {
            Ticket t = activeTickets.get(seat.getSeatId());

            // MẶC ĐỊNH: Ghế bị khóa (booked) nếu có Ticket của người khác hoặc đã CONFIRMED
            boolean isBooked = (t != null) &&
                    (t.getBooking().getStatus() == BookingStatus.CONFIRMED ||
                            !t.getBooking().getUser().getUserId().equals(currentUserId));

            // MẶC ĐỊNH: Ghế đang chọn (selecting) nếu là Ticket PENDING của chính mình
            boolean isSelecting = (t != null && t.getBooking().getStatus() == BookingStatus.PENDING
                    && t.getBooking().getUser().getUserId().equals(currentUserId))
                    || (selectingIds != null && selectingIds.contains(seat.getSeatId()));

            BigDecimal price = showTime.getBasePrice().multiply(seat.getSeatModifier() != null ? seat.getSeatModifier() : BigDecimal.ONE);

            return new SeatDTO(seat.getSeatId(), seat.getSeatName(), seat.getSeatLevel(), price, isBooked, isSelecting);
        }).collect(Collectors.toList());
    }

    // Chayj mỗi 5p - tự động set trạng thái suất chiếu
    // thành ngừng chiếu nếu giờ end_at < now
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void autoEndShowTime(){
        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả suất chiếu
        showTimeRepository.updateShowtimeStatus(now, ShowStatus.FINISHED);
    }


}
