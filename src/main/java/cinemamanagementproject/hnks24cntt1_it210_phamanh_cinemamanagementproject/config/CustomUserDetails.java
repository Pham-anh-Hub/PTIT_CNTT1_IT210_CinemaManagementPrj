package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.config;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getFullName() {
        return user.getProfile() != null ? user.getProfile().getFullName() : user.getUsername();
    }

    public String getAvatar() {
        return user.getProfile() != null ? user.getProfile().getAvatar() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.getStatus(); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus(); }
}