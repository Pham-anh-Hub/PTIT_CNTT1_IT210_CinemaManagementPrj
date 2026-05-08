package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProfileRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDay;

    @Size(max = 255)
    private String address;

    // File ảnh — không validate bắt buộc, để trống = giữ ảnh cũ
    private MultipartFile avatarFile;

    private String avatarUrl;
}