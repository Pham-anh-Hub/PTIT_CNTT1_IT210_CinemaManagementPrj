package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    // Lấy danh sách ID các ghế đã được đặt cho một suất chiếu cụ thể
    public List<Long> getBookedSeatIdsByShow(Long showId) {
        return ticketRepository.findByShowTime_ShowId(showId).stream()
                .map(ticket -> ticket.getSeat().getSeatId())
                .collect(Collectors.toList());
    }
}