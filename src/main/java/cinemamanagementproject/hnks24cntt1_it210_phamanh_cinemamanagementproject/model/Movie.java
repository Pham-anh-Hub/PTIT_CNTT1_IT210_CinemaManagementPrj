package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;
import cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter @Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "durations", nullable = false)
    private Integer durations;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "director")
    private String director;

    @Column(name = "casts", columnDefinition = "TEXT")
    private String casts;

    @Column(name = "rating_age")
    private String ratingAge;

    @Column(name = "released_date")
    private LocalDate releasedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MovieStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ===== RELATION =====

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genres_id")
    )
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie")
    private List<ShowTime> showTimes;
}