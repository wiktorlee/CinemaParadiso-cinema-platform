package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO dla godziny seansu w repertuarze
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningTimeDTO {
    
    private Long screeningId;
    private LocalTime startTime;
    private String roomNumber;
    private BigDecimal basePrice;
    private BigDecimal vipPrice;
    private Integer availableSeats;
    private Integer totalSeats;
}

