package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do oceniania filmu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateMovieDTO {
    
    @NotNull(message = "Ocena jest wymagana")
    @Min(value = 1, message = "Ocena musi być między 1 a 5")
    @Max(value = 5, message = "Ocena musi być między 1 a 5")
    private Integer rating;
}


