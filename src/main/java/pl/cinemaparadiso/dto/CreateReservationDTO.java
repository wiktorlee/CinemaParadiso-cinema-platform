package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.TicketType;

import java.util.List;

/**
 * DTO do tworzenia nowej rezerwacji
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationDTO {
    
    @NotNull(message = "ID seansu jest wymagane")
    @Positive(message = "ID seansu musi być dodatnie")
    private Long screeningId;
    
    @NotNull(message = "Lista miejsc jest wymagana")
    @Size(min = 1, message = "Musisz wybrać przynajmniej jedno miejsce")
    private List<SeatSelectionDTO> seats;
    
    /**
     * DTO reprezentujące wybór miejsca z typem biletu
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatSelectionDTO {
        
        @NotNull(message = "ID miejsca jest wymagane")
        @Positive(message = "ID miejsca musi być dodatnie")
        private Long seatId;
        
        @NotNull(message = "Typ biletu jest wymagany")
        private TicketType ticketType;
    }
}


