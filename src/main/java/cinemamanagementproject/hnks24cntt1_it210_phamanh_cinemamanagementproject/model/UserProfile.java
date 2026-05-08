package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

// entity/UserProfile.java
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "birthDay")
    private LocalDate birthDay;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "address")
    private String address;
}