package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.config;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.Role;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại"));

        return new CustomUserDetails(user);
    }

    private Collection<? extends GrantedAuthority> mapRoles(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}