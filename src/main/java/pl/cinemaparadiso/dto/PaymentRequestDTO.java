package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.PaymentMethod;

/**
 * DTO dla żądania płatności
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    
    @NotNull(message = "ID rezerwacji jest wymagane")
    private Long reservationId;
    
    @NotNull(message = "Metoda płatności jest wymagana")
    private PaymentMethod paymentMethod;
    
    // Dane karty (opcjonalne, tylko dla kart)
    private String cardNumber;
    private String expiryDate;  // Format: MM/RR
    private String cvv;
    
    // Dane BLIK (opcjonalne, tylko dla BLIK)
    private String blikCode;
    
    // Dane PayPal (opcjonalne, tylko dla PayPal)
    private String paypalEmail;
}

