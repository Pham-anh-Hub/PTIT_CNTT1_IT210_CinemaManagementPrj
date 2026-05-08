package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;

import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// repository/UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByProfilePhone(String phone);

}