package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.LoginDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.RegisterDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.IdGeneratorType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService userService;

    @GetMapping("/auth/login")
    public String loginPage(
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            HttpServletRequest request) {

        model.addAttribute("loginDTO", new LoginDTO());

        // Trường hợp sai mật khẩu/email
        if (error != null) {
            model.addAttribute("errorExist", "Sai email hoặc mật khẩu");
        }

        // Trường hợp vừa nhấn đăng xuất
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công");
        }

        return "auth/login_form";
    }

    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register_form";
    }
    @PostMapping("/auth/on-register")
    public String registerUser(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                               BindingResult bindingResult,
                               Model model) {

        // 1. Kiểm tra lỗi định dạng (Email, Trống, Độ dài mật khẩu)
        if (bindingResult.hasErrors()) {
            return "auth/register_form";
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "auth/register_form";
        }

        // 3. Kiểm tra trùng dữ liệu
        if (userService.existsByEmail(registerDTO.getEmail())) {
            model.addAttribute("errorMessage", "Email này đã được sử dụng!");
            return "auth/register_form";
        }

        if (userService.existsByPhone(registerDTO.getPhone())) {
            model.addAttribute("errorMessage", "Số điện thoại đã được sử dụng!");
            return "auth/register_form";
        }

        // 4. Lưu và chuyển hướng
        try {
            userService.register(registerDTO);
            return "redirect:/auth/login"; // Thêm param để hiện thông báo thành công
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "auth/register_form";
        }
    }
}