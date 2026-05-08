package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ShowTimeDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.ShowTime;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.IMovieRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.RoomRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.ShowTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// controller/admin/ShowTimeController.java
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/showtimes")
public class ShowTimeController {

    private final ShowTimeService showTimeService;
    private final IMovieRepository movieRepository;
    private final RoomRepository roomRepository;

    /** Danh sách suất chiếu */
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            Model model
    ) {
        // Gửi dữ liệu đã lọc về model
        model.addAttribute("showTimes", showTimeService.searchShowTimes(keyword, roomId, status));

        // Giữ lại các giá trị đã chọn trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("status", status);

        // Dữ liệu cho các dropdown
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("showingCount", showTimeService.getShowingNow());
        model.addAttribute("upcomingCount", showTimeService.getUpcoming());

        return "admin/admin-showtime-list";
    }

    /** Form tạo mới */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("showTimeDTO", new ShowTimeDTO());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("rooms",  roomRepository.findAll());
        return "showtime-form";
    }

    /** Xử lý tạo mới */
    @PostMapping("/create")
    public String doCreate(
            @ModelAttribute ShowTimeDTO dto,
            RedirectAttributes ra
    ) {
        try {
            showTimeService.createShowTime(dto);
            ra.addFlashAttribute("success", "Tạo suất chiếu thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/showtimes/create";
        }
        return "redirect:/admin/showtimes";
    }

    /** Form sửa */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ShowTime s = showTimeService.findById(id);

        ShowTimeDTO dto = new ShowTimeDTO();
        dto.setMovieId(s.getMovie().getMovieId());
        dto.setRoomId(s.getRoom().getRoomId());
        dto.setStartAt(s.getStartAt());
        dto.setBasePrice(s.getBasePrice());

        model.addAttribute("showTimeDTO", dto);
        model.addAttribute("showId",  id);
        model.addAttribute("movies",  movieRepository.findAll());
        model.addAttribute("rooms",   roomRepository.findAll());
        return "showtime-form";
    }

    /** Xử lý sửa */
    @PostMapping("/edit/{id}")
    public String doEdit(@PathVariable Long id,
                         @ModelAttribute ShowTimeDTO dto,
                         RedirectAttributes ra) {
        try {
            showTimeService.updateShowTime(id, dto);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/showtimes/edit/" + id;
        }
        return "redirect:/admin/showtimes";
    }

    /** Xóa */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            showTimeService.deleteShowTime(id);
            ra.addFlashAttribute("success", "Đã xóa suất chiếu!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }
}