package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.SeatType;

/**
 * DTO dla miejsca w sali
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    
    private Long id;
    private Long roomId;
    private Integer rowNumber;
    private Integer seatNumber;
    private SeatType seatType; // STANDARD lub VIP
    private Boolean isAvailable; // Czy miejsce jest dostępne
}

