package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.service;


import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileRequestDTO;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto.ProfileResponseDTO;

public interface IProfileService {
    ProfileResponseDTO getProfile(Long userId);
    ProfileResponseDTO updateProfile(Long userId, ProfileRequestDTO dto);
}