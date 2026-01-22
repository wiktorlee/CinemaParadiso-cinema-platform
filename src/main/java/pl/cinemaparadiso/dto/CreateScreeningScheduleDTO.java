package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotNull;
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
 * DTO do tworzenia harmonogramu seansów
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScreeningScheduleDTO {
    
    @NotNull(message = "Film jest wymagany")
    private Long movieId;
    
    @NotNull(message = "Sala jest wymagana")
    private Long roomId;
    
    @NotNull(message = "Dzień tygodnia jest wymagany")
    private ScheduleDayOfWeek dayOfWeek;
    
    @NotNull(message = "Godzina rozpoczęcia jest wymagana")
    private LocalTime startTime;
    
    @NotNull(message = "Data rozpoczęcia jest wymagana")
    private LocalDate startDate;
    
    @NotNull(message = "Data zakończenia jest wymagana")
    private LocalDate endDate;
    
    @NotNull(message = "Cena bazowa jest wymagana")
    @Positive(message = "Cena bazowa musi być dodatnia")
    private BigDecimal basePrice;
    
    @Positive(message = "Cena VIP musi być dodatnia")
    private BigDecimal vipPrice; // Opcjonalne
}

