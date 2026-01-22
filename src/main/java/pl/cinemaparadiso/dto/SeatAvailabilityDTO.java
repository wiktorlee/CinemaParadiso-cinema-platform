package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.SeatType;

/**
 * DTO reprezentujące dostępność miejsca dla seansu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityDTO {
    
    private Long seatId;
    private Integer rowNumber;
    private Integer seatNumber;
    private SeatType seatType; // STANDARD lub VIP
    private Boolean isAvailable; // Czy miejsce jest dostępne (niezarezerwowane)
    private Boolean isSeatEnabled; // Czy miejsce jest włączone (nie uszkodzone)
}


