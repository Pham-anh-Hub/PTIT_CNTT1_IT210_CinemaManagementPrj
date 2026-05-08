package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// config/AppConfig.java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

    private final CustomUserDetailsService userDetailsService;
    private final CustomSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/cinema/movie/detail/**", "/auth/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // QUAN TRỌNG: Mọi thao tác đặt vé và chọn ghế phải đăng nhập
                        .requestMatchers("/cinema/booking/**").authenticated()
                        .requestMatchers("/cinema/profile/**", "/cinema/orders/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/auth/login") // Trang hiện form
                        .loginProcessingUrl("/cinema/auth/login") // Link submit form
                        .usernameParameter("email")
                        .passwordParameter("password")

                        // Sử dụng defaultSuccessUrl mà không có tham số 'true'
                        // để nó ưu tiên quay lại trang mà người dùng đã bị chặn trước đó (ví dụ trang chọn ghế)
                        .defaultSuccessUrl("/home", false)

                        // Nếu đăng nhập thất bại, ép nó quay về trang login của bạn kèm lỗi
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // Khớp với link logout trong navbar
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ /images/** vào đúng thư mục vật lý nơi bạn save file
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/JavaWeb App/HN-KS24-CNTT1_IT210_PhamAnh_CinemaManagementProject/src/main/resources/static/images/");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}