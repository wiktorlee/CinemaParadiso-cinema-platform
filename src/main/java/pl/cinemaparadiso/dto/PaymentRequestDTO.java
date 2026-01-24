package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    
    @NotNull(message = "ID rezerwacji jest wymagane")
    private Long reservationId;
    
    @NotNull(message = "Metoda płatności jest wymagana")
    private PaymentMethod paymentMethod;
    
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    
    private String blikCode;
    
    private String paypalEmail;
}

