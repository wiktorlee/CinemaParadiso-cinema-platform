package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do tworzenia/aktualizacji recenzji
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDTO {
    
    @NotBlank(message = "Treść recenzji jest wymagana")
    @Size(min = 10, max = 5000, message = "Recenzja musi mieć między 10 a 5000 znaków")
    private String content;
}


