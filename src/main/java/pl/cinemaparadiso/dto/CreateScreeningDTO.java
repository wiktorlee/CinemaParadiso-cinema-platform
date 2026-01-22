package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do tworzenia nowego seansu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScreeningDTO {
    
    @NotNull(message = "Film jest wymagany")
    private Long movieId;
    
    @NotNull(message = "Sala jest wymagana")
    private Long roomId;
    
    @NotNull(message = "Data i godzina rozpoczęcia są wymagane")
    private LocalDateTime startTime;
    
    @NotNull(message = "Cena bazowa jest wymagana")
    @Positive(message = "Cena bazowa musi być dodatnia")
    private BigDecimal basePrice;
    
    @Positive(message = "Cena VIP musi być dodatnia")
    private BigDecimal vipPrice; // Opcjonalne
}

