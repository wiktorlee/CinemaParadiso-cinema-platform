package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pl.cinemaparadiso.dto.PaymentRequestDTO;
import pl.cinemaparadiso.dto.PaymentResponseDTO;
import pl.cinemaparadiso.entity.Reservation;
import pl.cinemaparadiso.enums.PaymentMethod;
import pl.cinemaparadiso.enums.ReservationStatus;
import pl.cinemaparadiso.exception.ReservationNotFoundException;
import pl.cinemaparadiso.exception.SeatNotAvailableException;
import pl.cinemaparadiso.repository.ReservationRepository;
import pl.cinemaparadiso.service.ReservationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final Random random = new Random();
    
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest, Long userId) {
        log.info("Przetwarzanie płatności dla rezerwacji ID: {} przez użytkownika ID: {}, metoda: {}", 
                paymentRequest.getReservationId(), userId, paymentRequest.getPaymentMethod());
        
        Reservation reservation = reservationRepository.findByIdWithLock(paymentRequest.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Rezerwacja o ID " + paymentRequest.getReservationId() + " nie istnieje"));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do płatności za tę rezerwację");
        }
        
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT && 
            reservation.getStatus() != ReservationStatus.PAYMENT_FAILED) {
            throw new IllegalArgumentException(
                    "Rezerwacja nie oczekuje na płatność. Status: " + reservation.getStatus());
        }
        
        try {
            reservationService.verifySeatsAvailability(reservation);
            log.debug("Weryfikacja dostępności miejsc zakończona pomyślnie dla rezerwacji ID: {}", 
                    reservation.getId());
        } catch (SeatNotAvailableException e) {
            log.warn("Nie można zrealizować płatności - miejsca są niedostępne. Rezerwacja ID: {}, Błąd: {}", 
                    reservation.getId(), e.getMessage());
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            throw new IllegalArgumentException(
                    "Nie można zrealizować płatności. " + e.getMessage() + 
                    " Rezerwacja została anulowana. Proszę wybrać inne miejsca.");
        }
        
        if (paymentRequest.getPaymentMethod() == PaymentMethod.CASH) {
            String transactionId = generateTransactionId();
            BigDecimal amount = reservation.getTotalPrice();
            
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaymentMethod(PaymentMethod.CASH);
            reservation.setPaymentDate(LocalDateTime.now());
            reservation.setPaymentTransactionId(transactionId);
            reservationRepository.save(reservation);
            
            log.info("Płatność gotówką zakończona sukcesem. Rezerwacja ID: {}, Transakcja ID: {}", 
                    reservation.getId(), transactionId);
            
            return PaymentResponseDTO.builder()
                    .reservationId(reservation.getId())
                    .success(true)
                    .message("Rezerwacja została potwierdzona. Płatność gotówką w kasie kina.")
                    .paymentMethod(PaymentMethod.CASH)
                    .transactionId(transactionId)
                    .paymentDate(reservation.getPaymentDate())
                    .amount(amount)
                    .build();
        }
        
        validatePaymentData(paymentRequest);
        
        simulateProcessingDelay();
        
        boolean paymentSuccess = random.nextDouble() < 0.9;
        
        if (paymentSuccess) {
            String transactionId = generateTransactionId();
            BigDecimal amount = reservation.getTotalPrice();
            
            Reservation finalReservation = reservation;
            try {
                finalReservation.setStatus(ReservationStatus.PAID);
                finalReservation.setPaymentMethod(paymentRequest.getPaymentMethod());
                finalReservation.setPaymentDate(LocalDateTime.now());
                finalReservation.setPaymentTransactionId(transactionId);
                reservationRepository.save(finalReservation);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.error("Błąd optymistycznego blokowania podczas płatności. Rezerwacja ID: {} została zmodyfikowana przez inny proces.", 
                        finalReservation.getId());
                try {
                    reservationService.verifySeatsAvailability(finalReservation);
                    Reservation freshReservation = reservationRepository.findByIdWithLock(finalReservation.getId())
                            .orElseThrow(() -> new ReservationNotFoundException(
                                    "Rezerwacja o ID " + finalReservation.getId() + " nie istnieje"));
                    if (freshReservation.getStatus() == ReservationStatus.PENDING_PAYMENT || 
                        freshReservation.getStatus() == ReservationStatus.PAYMENT_FAILED) {
                        freshReservation.setStatus(ReservationStatus.PAID);
                        freshReservation.setPaymentMethod(paymentRequest.getPaymentMethod());
                        freshReservation.setPaymentDate(LocalDateTime.now());
                        freshReservation.setPaymentTransactionId(transactionId);
                        reservationRepository.save(freshReservation);
                        reservation = freshReservation;
                        log.info("Ponowna próba płatności zakończona sukcesem. Rezerwacja ID: {}", reservation.getId());
                    } else {
                        throw new IllegalArgumentException("Rezerwacja została już przetworzona. Status: " + freshReservation.getStatus());
                    }
                } catch (SeatNotAvailableException ex) {
                    log.error("Miejsca są niedostępne po błędzie optymistycznego blokowania. Rezerwacja ID: {}", 
                            finalReservation.getId());
                    finalReservation.setStatus(ReservationStatus.CANCELLED);
                    reservationRepository.save(finalReservation);
                    throw new IllegalArgumentException(
                            "Nie można zrealizować płatności. " + ex.getMessage() + 
                            " Rezerwacja została anulowana. Proszę wybrać inne miejsca.");
                }
            }
            
            log.info("Płatność zakończona sukcesem. Rezerwacja ID: {}, Transakcja ID: {}", 
                    reservation.getId(), transactionId);
            
            return PaymentResponseDTO.builder()
                    .reservationId(reservation.getId())
                    .success(true)
                    .message("Płatność zakończona sukcesem")
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .transactionId(transactionId)
                    .paymentDate(reservation.getPaymentDate())
                    .amount(amount)
                    .build();
        } else {
            reservation.setStatus(ReservationStatus.PAYMENT_FAILED);
            reservationRepository.save(reservation);
            
            log.warn("Płatność nie powiodła się. Rezerwacja ID: {}", reservation.getId());
            
            return PaymentResponseDTO.builder()
                    .reservationId(reservation.getId())
                    .success(false)
                    .message("Płatność nie powiodła się. Spróbuj ponownie.")
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .amount(reservation.getTotalPrice())
                    .build();
        }
    }
    
    private void validatePaymentData(PaymentRequestDTO request) {
        PaymentMethod method = request.getPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("Numer karty jest wymagany");
                }
                String cardNumber = request.getCardNumber().replaceAll("\\s", "");
                if (!cardNumber.matches("\\d{13,19}")) {
                    throw new IllegalArgumentException("Numer karty musi zawierać 13-19 cyfr");
                }
                
                if (request.getExpiryDate() == null || !request.getExpiryDate().matches("\\d{2}/\\d{2}")) {
                    throw new IllegalArgumentException("Data ważności musi być w formacie MM/RR");
                }
                
                if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
                    throw new IllegalArgumentException("CVV musi zawierać 3-4 cyfry");
                }
                break;
                
            case BLIK:
                if (request.getBlikCode() == null || !request.getBlikCode().matches("\\d{6}")) {
                    throw new IllegalArgumentException("Kod BLIK musi zawierać 6 cyfr");
                }
                break;
                
            case PAYPAL:
                if (request.getPaypalEmail() == null || !request.getPaypalEmail().contains("@")) {
                    throw new IllegalArgumentException("Nieprawidłowy adres email PayPal");
                }
                break;
                
            case CASH:
                break;
                
            case MOCK:
                break;
                
            default:
                throw new IllegalArgumentException("Nieobsługiwana metoda płatności: " + method);
        }
    }
    
    private void simulateProcessingDelay() {
        try {
            int delay = 1000 + random.nextInt(2000);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Przerwano symulację opóźnienia płatności");
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }
}

