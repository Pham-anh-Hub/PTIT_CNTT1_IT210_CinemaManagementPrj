package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.repository;


import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_UserId(Long userId);
}