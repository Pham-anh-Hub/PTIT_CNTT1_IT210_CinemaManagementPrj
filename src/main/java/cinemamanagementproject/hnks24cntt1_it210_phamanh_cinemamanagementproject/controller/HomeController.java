package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.IMovieRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final IMovieRepository movieRepository;
    private final UserRepository userRepository;

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("movieNowShowing", movieRepository.getMovieByStatus(MovieStatus.NOW_SHOWING));
        model.addAttribute("movieUpcoming", movieRepository.getMovieByStatus(MovieStatus.UPCOMING));
        return "home_page";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-movie-list";
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        return "home_for_staff";
    }

    @GetMapping("/user/booking-list")
    public String viewBookingHistory(){

        
        return "booking-history";
    }


}
