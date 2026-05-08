package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

// dto/RegisterDTO.java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RegisterDTO {
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        private String password;

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        private String confirmPassword;

        @NotBlank(message = "Họ không được để trống")
        private String firstName;

        @NotBlank(message = "Tên không được để trống")
        private String lastName;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải có 10 chữ số")
        private String phone;

}