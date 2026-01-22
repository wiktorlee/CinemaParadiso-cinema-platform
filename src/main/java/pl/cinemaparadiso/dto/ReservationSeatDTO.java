package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.TicketType;

import java.math.BigDecimal;

/**
 * DTO dla miejsca w rezerwacji
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSeatDTO {
    
    private Long id;
    private Long seatId;
    private Integer rowNumber;
    private Integer seatNumber;
    private TicketType ticketType;
    private BigDecimal price;
}


