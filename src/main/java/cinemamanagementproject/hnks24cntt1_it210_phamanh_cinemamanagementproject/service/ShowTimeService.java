package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.SeatDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ShowTimeDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.*;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
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


    public List<ShowTime> searchShowTimes(String keyword, Long roomId, String status) {
        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả kèm movie/room để tránh lỗi N+1 khi filter và render
        return showTimeRepository.findAllWithDetails().stream()
                .filter(s -> {
                    // Lọc theo từ khóa (Tên phim)
                    boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                            s.getMovie().getMovieTitle().toLowerCase().contains(keyword.toLowerCase());

                    // Lọc theo phòng chiếu
                    boolean matchRoom = (roomId == null) ||
                            s.getRoom().getRoomId().equals(roomId);

                    // Lọc theo trạng thái (Sử dụng logic thời gian)
                    boolean matchStatus = true;
                    if ("SHOWING".equalsIgnoreCase(status)) {
                        matchStatus = now.isAfter(s.getStartAt()) && now.isBefore(s.getEndedAt());
                    } else if ("UPCOMING".equalsIgnoreCase(status)) {
                        matchStatus = now.isBefore(s.getStartAt());
                    } else if ("ENDED".equalsIgnoreCase(status)) {
                        matchStatus = now.isAfter(s.getEndedAt());
                    }

                    return matchKeyword && matchRoom && matchStatus;
                })
                .collect(Collectors.toList());
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
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim!"));

        // 2. Lấy thông tin Room
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu!"));

        // 3. Tính toán endedAt = startAt + duration (phút) + 15 phút dọn phòng (tùy chọn)
        LocalDateTime startAt = dto.getStartAt();
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
            ShowTime firstConflict = conflicts.get(0);
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

        // 4. KIỂM TRA XUNG ĐỘT (QUAN TRỌNG: Truyền chính ID hiện tại vào excludeId)
        // Việc truyền 'id' vào giúp SQL bỏ qua chính bản ghi này khi kiểm tra chồng chéo
        List<ShowTime> conflicts = showTimeRepository.findConflicts(
                room.getRoomId(),
                startAt,
                endedAt,
                id // <--- Thay -1L bằng id của suất chiếu đang sửa
        );

        if (!conflicts.isEmpty()) {
            ShowTime firstConflict = conflicts.get(0);
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

    public void deleteShowTime(Long id) {
        // 1. Kiểm tra sự tồn tại
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu để xóa!"));

        // 2. KIỂM TRA RÀNG BUỘC: Đã có vé nào được đặt cho suất này chưa?
        boolean hasTickets = !ticketRepository.findByShowTime_ShowId(id).isEmpty();

        if (hasTickets) {
            throw new RuntimeException("Không thể xóa suất chiếu này vì đã có khách hàng đặt vé! " +
                    "Vui lòng hủy các vé liên quan trước khi thực hiện.");
        }

        // 3. Nếu không có ràng buộc, tiến hành xóa
        try {
            showTimeRepository.delete(showTime);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi xóa suất chiếu: " + e.getMessage());
        }
    }

    public List<SeatDTO> getSeatMapForShow(Long showId, List<Long> selectingIds) {
        ShowTime showTime = showTimeRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu"));

        Room room = showTime.getRoom();

        // 1. Lấy tất cả ghế đang có trong DB (Dù là 15 hay 40)
        List<Seat> seatsInDb = seatRepository.findByRoom_RoomId(room.getRoomId());

        // 2. Bỏ qua việc kiểm tra khớp số lượng.
        // Hệ thống sẽ chỉ hiện bấy nhiêu ghế có trong danh sách seatsInDb.

        // 3. Logic lấy vé đã đặt
        Set<Long> bookedSeatIds = ticketRepository.findByShowTime_ShowId(showId)
                .stream().map(t -> t.getSeat().getSeatId()).collect(Collectors.toSet());

        // 4. Map ra DTO
        return seatsInDb.stream().map(seat -> {
            boolean isBooked = bookedSeatIds.contains(seat.getSeatId());
            boolean isSelecting = selectingIds != null && selectingIds.contains(seat.getSeatId());

            BigDecimal price = showTime.getBasePrice().add(
                    seat.getSeatModifier() != null ? seat.getSeatModifier() : BigDecimal.ZERO
            );

            return new SeatDTO(seat.getSeatId(), seat.getSeatName(),
                    seat.getSeatLevel(), price, isBooked, isSelecting);
        }).collect(Collectors.toList());
    }}
