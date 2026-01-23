package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dla odpowiedzi z płatności
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    
    private Long reservationId;
    private boolean success;
    private String message;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
}


