package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do aktualizacji seansu
 * Tylko niektóre pola można edytować (nie można zmienić filmu/sali jeśli są już rezerwacje)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScreeningDTO {
    
    private LocalDateTime startTime;
    
    @Positive(message = "Cena bazowa musi być dodatnia")
    private BigDecimal basePrice;
    
    @Positive(message = "Cena VIP musi być dodatnia")
    private BigDecimal vipPrice;
}

