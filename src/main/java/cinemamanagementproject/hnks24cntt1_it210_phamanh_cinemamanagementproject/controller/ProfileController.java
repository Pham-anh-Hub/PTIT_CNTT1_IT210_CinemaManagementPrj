package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.controller;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.config.CustomUserDetails;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.Role;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service.IProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cinema/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;
    private final UserRepository  userRepository;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        Long userId = getCurrentUserId(authentication);
        ProfileResponseDTO profile = profileService.getProfile(userId);

        ProfileRequestDTO form = new ProfileRequestDTO();
        form.setFullName(profile.getFullName());
        form.setPhone(profile.getPhone());
        form.setBirthDay(profile.getBirthDay());
        form.setAddress(profile.getAddress());

        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", form);
        return "detail_user";
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileRequestDTO dto,
            BindingResult bindingResult,
            // Nhận file riêng để tránh conflict với @ModelAttribute
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = getCurrentUserId(authentication);

        // Gán file vào DTO
        if (avatarFile != null && !avatarFile.isEmpty()) {
            dto.setAvatarFile(avatarFile);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profileService.getProfile(userId));
            return "detail_user";
        }

        profileService.updateProfile(userId, dto);
        refreshSecurityContext(userId);

        redirectAttributes.addFlashAttribute("successMsg", "Cập nhật hồ sơ thành công!");
        if (profileService.getProfile(userId).getRole().equals(Role.ADMIN)){
            return "redirect:/admin/dashboard";
        } else if (profileService.getProfile(userId).getRole().equals(Role.STAFF)) {
            return "redirect:/staff/home";
        }
        return "redirect:/home";
    }

    private Long getCurrentUserId(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getUser().getUserId();
    }

    private void refreshSecurityContext(Long userId) {
        User freshUser = userRepository.findById(userId).orElseThrow();
        CustomUserDetails freshDetails = new CustomUserDetails(freshUser);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                freshDetails, freshDetails.getPassword(), freshDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}