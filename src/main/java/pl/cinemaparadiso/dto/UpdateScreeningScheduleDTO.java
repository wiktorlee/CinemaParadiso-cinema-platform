package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.ScheduleDayOfWeek;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO do aktualizacji harmonogramu seansów
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScreeningScheduleDTO {
    
    private ScheduleDayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Positive(message = "Cena bazowa musi być dodatnia")
    private BigDecimal basePrice;
    
    @Positive(message = "Cena VIP musi być dodatnia")
    private BigDecimal vipPrice;
}

