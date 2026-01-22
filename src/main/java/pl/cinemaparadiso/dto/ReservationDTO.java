package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO dla rezerwacji
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private Long screeningId;
    private String movieTitle;
    private String roomNumber;
    private LocalDateTime screeningStartTime;
    private LocalDateTime createdAt;
    private ReservationStatus status;
    private BigDecimal totalPrice;
    private List<ReservationSeatDTO> seats; // Lista zarezerwowanych miejsc
}


