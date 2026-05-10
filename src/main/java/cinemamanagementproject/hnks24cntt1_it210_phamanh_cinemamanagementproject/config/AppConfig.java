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
                        // 1. Cho phép tài nguyên tĩnh + Public (PHẢI LÊN ĐẦU)
                        .requestMatchers("/",
                                "/home",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/cinema/movie/detail/**").permitAll()

                        // 2. Cho phép các trang công khai cụ thể
                        .requestMatchers("/", "/home", "/auth/**", "/cinema/movie/detail/**").permitAll()

                        // 3. Chặn các trang cụ thể yêu cầu đăng nhập
                        .requestMatchers("/user/booking-list", "/cinema/booking/**", "/cinema/profile/**").authenticated()

                        // 4. Các trang còn lại: Nếu muốn mở cửa thì để permitAll, muốn bảo mật thì authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/cinema/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
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