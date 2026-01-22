package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO do aktualizacji filmu
 * Wszystkie pola są opcjonalne (null oznacza, że pole nie jest aktualizowane)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieDTO {
    
    private String title;
    
    private String description;
    
    private String genre;
    
    private String director;
    
    @Positive(message = "Czas trwania musi być dodatni")
    private Integer durationMinutes;
    
    private LocalDate releaseDate;
    
    private Integer year;
    
    // posterPath nie jest tutaj - będzie aktualizowany przez osobny endpoint uploadu
}


