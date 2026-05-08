package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.RegisterDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.Role;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.UserProfile;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp!");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(true);

        UserProfile profile = new UserProfile();
        profile.setFullName(dto.getLastName() + " " + dto.getFirstName());
        profile.setPhone(dto.getPhone());
        profile.setUser(user);

        user.setProfile(profile);

        userRepository.save(user);
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone){
        return userRepository.existsByProfilePhone(phone);
    }


}