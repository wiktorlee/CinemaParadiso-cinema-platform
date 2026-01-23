package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.PaymentRequestDTO;
import pl.cinemaparadiso.dto.PaymentResponseDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.service.PaymentService;

/**
 * Controller dla endpointów związanych z płatnościami
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Przetwarza płatność za rezerwację
     * 
     * POST /api/payments/process
     * Wymaga zalogowania
     * 
     * @param paymentRequest - dane płatności (walidowane przez @Valid)
     * @return odpowiedź z wynikiem płatności
     */
    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO paymentRequest) {
        Long userId = getCurrentUserId();
        log.info("Żądanie płatności przez użytkownika ID: {} dla rezerwacji ID: {}", 
                userId, paymentRequest.getReservationId());
        
        try {
            PaymentResponseDTO response = paymentService.processPayment(paymentRequest, userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Błąd walidacji płatności: {}", e.getMessage());
            PaymentResponseDTO errorResponse = PaymentResponseDTO.builder()
                    .reservationId(paymentRequest.getReservationId())
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Pobiera ID zalogowanego użytkownika z SecurityContext
     * 
     * @return ID użytkownika
     * @throws IllegalStateException jeśli użytkownik nie jest zalogowany
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Użytkownik nie jest zalogowany");
        }
        
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}


