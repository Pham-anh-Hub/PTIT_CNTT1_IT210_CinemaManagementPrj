package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.config.CustomUserDetails;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileResponseDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.UserProfile;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserProfileRepository;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // Thư mục lưu ảnh — khớp với addResourceHandlers trong AuthConfig
    private static final String UPLOAD_DIR =
            "D:\\JavaWeb App\\HN-KS24-CNTT1_IT210_PhamAnh_CinemaManagementProject\\src\\main\\resources\\static\\images\\";

    @Override
    public ProfileResponseDTO getProfile(Long userId) {
        return toResponseDTO(findUser(userId));
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(Long userId, ProfileRequestDTO dto) {
        User user = findUser(userId);

        UserProfile profile = userProfileRepository
                .findByUser_UserId(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });

        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setBirthDay(dto.getBirthDay());
        profile.setAddress(dto.getAddress());

        // Xử lý file ảnh nếu user có chọn ảnh mới
        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            String filename = saveAvatarFile(dto.getAvatarFile(), userId);
            // CHỈ lưu tên file, không lưu kèm đường dẫn cứng
            profile.setAvatar(filename);
        }

        userProfileRepository.save(profile);
        user.setProfile(profile);
        return toResponseDTO(user);
    }

    // ── Lưu file vật lý + trả về tên file ────────────────────────
    private String saveAvatarFile(MultipartFile file, Long userId) {
        try {
            // 1. Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Lấy tên file gốc (ví dụ: "my_photo.png")
            String originalFullFileName = file.getOriginalFilename();
            if (originalFullFileName == null || originalFullFileName.isEmpty()) {
                originalFullFileName = "default.jpg";
            }

            // 3. Tách tên và phần mở rộng
            String fileNameOnly = "";
            String extension = "";

            int dotIndex = originalFullFileName.lastIndexOf(".");
            if (dotIndex > 0) {
                fileNameOnly = originalFullFileName.substring(0, dotIndex);
                extension = originalFullFileName.substring(dotIndex); // ví dụ: ".png"
            } else {
                fileNameOnly = originalFullFileName;
                extension = ".jpg";
            }

            // 4. Tạo tên file mới: [tên_gốc]_[timestamp][extension]
            // Ví dụ: my_photo_1715173000123.png
            String finalFileName = fileNameOnly + "_" + System.currentTimeMillis() + extension;

            // 5. Lưu file vào thư mục vật lý
            Files.copy(file.getInputStream(),
                    uploadPath.resolve(finalFileName),
                    StandardCopyOption.REPLACE_EXISTING);

            return finalFileName;

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu ảnh đại diện với tên gốc", e);
        }
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id=" + userId));
    }

    private ProfileResponseDTO toResponseDTO(User user) {
        UserProfile p = user.getProfile();
        return ProfileResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .fullName(p != null ? p.getFullName() : null)
                .phone(p    != null ? p.getPhone()    : null)
                .birthDay(p != null ? p.getBirthDay() : null)
                .avatar(p   != null ? p.getAvatar()   : null)
                .address(p  != null ? p.getAddress()  : null)
                .build();
    }

    public Long getCurrentUserId(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getUser().getUserId();
    }

    public User getCurrentUser(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getUser();
    }
}