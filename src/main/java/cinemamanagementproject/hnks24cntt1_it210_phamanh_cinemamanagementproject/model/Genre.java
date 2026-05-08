package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "genres")
@Getter
@Setter
public class Genre implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genres_id")
    private Long genresId;

    @Column(name = "genres_name")
    private String genresName;
    @Column(name = "genres_desc")
    private String genresDesc;

    @ManyToMany(mappedBy = "genres")
    private List<Movie> movies;
}