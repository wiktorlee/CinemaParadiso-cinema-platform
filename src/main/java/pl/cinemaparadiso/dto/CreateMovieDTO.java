package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO do tworzenia nowego filmu
 * Zawiera walidację danych wejściowych
 * 
 * @NotBlank - pole nie może być puste ani null
 * @NotNull - pole nie może być null
 * @Positive - wartość musi być dodatnia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieDTO {
    
    @NotBlank(message = "Tytuł filmu jest wymagany")
    private String title;
    
    private String description;
    
    private String genre;
    
    private String director;
    
    @NotNull(message = "Czas trwania jest wymagany")
    @Positive(message = "Czas trwania musi być dodatni")
    private Integer durationMinutes;
    
    private LocalDate releaseDate;
    
    private Integer year;
    
    // posterPath nie jest tutaj - będzie dodawany przez osobny endpoint uploadu
}


