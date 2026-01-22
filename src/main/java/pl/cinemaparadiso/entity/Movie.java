package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca film w kinie
 * 
 * @Entity - encja JPA
 * @Table(name = "movies") - tabela "movies" w bazie danych
 */
@Entity
@Table(name = "movies")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String genre; // Gatunek filmu (np. "Akcja", "Komedia", "Dramat")

    private String director; // Reżyser

    @Column(nullable = false)
    private Integer durationMinutes; // Czas trwania w minutach

    private LocalDate releaseDate; // Data premiery

    private Integer year; // Rok produkcji

    /**
     * Ścieżka do pliku okładki filmu
     * Przechowywana na dysku serwera, w bazie tylko ścieżka (np. "/images/movies/inception.jpg")
     * null jeśli film nie ma okładki
     */
    @Column(name = "poster_path")
    private String posterPath;

    /**
     * @OneToMany - jeden film może mieć wiele seansów
     * mappedBy = "movie" - pole w klasie Screening, które wskazuje na ten film
     * cascade = CascadeType.ALL - operacje na filmie wpływają na seanse
     * orphanRemoval = true - jeśli usuniemy film, usuną się też seanse
     * 
     * List<Screening> - lista seansów dla tego filmu
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Screening> screenings = new ArrayList<>();

    /**
     * @OneToMany - jeden film może mieć wiele harmonogramów
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScreeningSchedule> schedules = new ArrayList<>();
}

