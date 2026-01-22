package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dla seansu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDTO {
    
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Integer movieDurationMinutes;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime; // Obliczane: startTime + movieDurationMinutes
    private BigDecimal basePrice;
    private BigDecimal vipPrice;
    private Long scheduleId; // null jeśli utworzony ręcznie
    private Integer availableSeats; // Liczba dostępnych miejsc (będzie obliczane później)
    private Integer totalSeats; // Całkowita liczba miejsc w sali
}

