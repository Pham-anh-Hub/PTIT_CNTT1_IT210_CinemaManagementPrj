package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MonthRevenueDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.MovieRevenueDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.BookingStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.BookingRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

     private final BookingRepository bookingRepository;
     private final UserRepository userRepository;

    public Map<String, Object> getAnalyseData(){
        Map<String, Object> analyseData = new HashMap<String, Object>();

        // dữ liệu đếm & thống kê
        analyseData.put("totalRevenue", bookingRepository.getTotalRevenue());
        analyseData.put("totalConfirmBooking", bookingRepository.countBookingByStatus(BookingStatus.CONFIRMED));
        analyseData.put("totalPendingBooking", bookingRepository.countBookingByStatus(BookingStatus.PAID)); // đã thanh toán, chờ xác nhận
        analyseData.put("totalCustomers", userRepository.getTotalCustomer());


        // doanh thu theo tháng
        List<Object[]> revenueMonthly = bookingRepository.getMonthRevenue();

        Map<Integer, BigDecimal> monthRevenueMap = new LinkedHashMap<>();
        for (Object[] row : revenueMonthly){
            if(row[0] instanceof Number monthNum){
                monthRevenueMap.put(monthNum.intValue(), new BigDecimal(row[1].toString()));
            }
        }
        // doanh thu cao nhất
        BigDecimal maxRevenue = monthRevenueMap.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);

        // Tên tháng
        String[] monthNames = {"", "Tháng 1","Tháng 2","Tháng 3","Tháng 4",
                "Tháng 5","Tháng 6","Tháng 7","Tháng 8",
                "Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        List<MonthRevenueDTO> monthRevenueDTOList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            BigDecimal revenue = monthRevenueMap.getOrDefault(i, BigDecimal.ZERO);
            int percent = revenue.compareTo(BigDecimal.ZERO) == 0 ? 0 : revenue.multiply(BigDecimal.valueOf(100)).divide(maxRevenue, 0, RoundingMode.HALF_UP).intValue();
            monthRevenueDTOList.add(new MonthRevenueDTO(i, revenue, percent, monthNames[i]));
        }

        analyseData.put("monthRevenue", monthRevenueDTOList);

        // Top 5 phim
        List<Object[]> ratingTop5Movie = bookingRepository.getTop5Movie();

        // Doanh thu phim cao nhaats
        BigDecimal maxMovie = ratingTop5Movie.isEmpty() ? BigDecimal.ONE : new BigDecimal(ratingTop5Movie.getFirst()[1].toString());

        List<MovieRevenueDTO> top5MovieRevenue = new ArrayList<>();
        for (Object[] row : ratingTop5Movie) {
            String title = (String) row[0];
            BigDecimal revenue = new BigDecimal(row[1].toString());
            int bookingCount = ((Number) row[2]).intValue();
            int percent = revenue.multiply(BigDecimal.valueOf(100)).divide(maxMovie, 0, RoundingMode.HALF_UP).intValue();
            top5MovieRevenue.add(new MovieRevenueDTO(title, revenue, bookingCount, percent));
        }
        analyseData.put("top5Movies", top5MovieRevenue);
        return analyseData;
    }


}
