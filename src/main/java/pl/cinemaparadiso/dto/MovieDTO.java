package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dla filmu
 * Używany do transferu danych między warstwami (Service → Controller → Frontend)
 * 
 * DTO zawiera tylko te pola, które chcemy pokazać w API (bez wrażliwych danych)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String director;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private Integer year;
    private String posterPath; // Ścieżka do okładki (np. "/images/movies/inception.jpg")
}


