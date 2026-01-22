package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.ScheduleDayOfWeek;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO dla harmonogramu seansów
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningScheduleDTO {
    
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long roomId;
    private String roomNumber;
    private ScheduleDayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal basePrice;
    private BigDecimal vipPrice;
    private Integer generatedScreeningsCount; // Liczba wygenerowanych seansów
}

