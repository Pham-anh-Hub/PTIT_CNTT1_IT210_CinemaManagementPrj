package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;


import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.Role;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ProfileResponseDTO {

    // Từ bảng users
    private Long   userId;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;

    // Từ bảng user_profiles
    private String    fullName;
    private String    phone;
    private LocalDate birthDay;
    private String    avatar;
    private String    address;
}