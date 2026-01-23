package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.PaymentRequestDTO;
import pl.cinemaparadiso.dto.PaymentResponseDTO;
import pl.cinemaparadiso.entity.Reservation;
import pl.cinemaparadiso.enums.PaymentMethod;
import pl.cinemaparadiso.enums.ReservationStatus;
import pl.cinemaparadiso.exception.ReservationNotFoundException;
import pl.cinemaparadiso.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Serwis do obsługi płatności (symulacja)
 * 
 * SYMULACJA PŁATNOŚCI:
 * - Waliduje format danych płatności (długość, cyfry)
 * - Nie sprawdza rzeczywistych danych (Luhn, wygaśnięcie, środki)
 * - Losowo decyduje o sukcesie (90% sukcesu, 10% błąd)
 * - Generuje ID transakcji
 * - Aktualizuje status rezerwacji
 * 
 * BEZPIECZEŃSTWO I TRANSAKCYJNOŚĆ:
 * - @Transactional - wszystkie operacje są atomowe (all-or-nothing)
 * - Pessimistic locking - zapobiega race conditions podczas równoczesnych płatności
 * - Automatyczny rollback przy błędzie
 * - Weryfikacja własności rezerwacji przed płatnością
 * - Sprawdzanie statusu rezerwacji po locku (zapobiega double-payment)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional  // Wszystkie metody są transakcyjne - automatyczny rollback przy błędzie
public class PaymentService {
    
    private final ReservationRepository reservationRepository;
    private final Random random = new Random();
    
    /**
     * Przetwarza płatność (symulacja)
     * 
     * Metoda jest transakcyjna (@Transactional na klasie):
     * - Wszystkie operacje są atomowe (all-or-nothing)
     * - W przypadku błędu automatyczny rollback
     * - Używa pessimistic locking do zapobiegania race conditions
     * 
     * @param paymentRequest - dane płatności
     * @param userId - ID użytkownika (do weryfikacji własności rezerwacji)
     * @return odpowiedź z wynikiem płatności
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest, Long userId) {
        log.info("Przetwarzanie płatności dla rezerwacji ID: {} przez użytkownika ID: {}, metoda: {}", 
                paymentRequest.getReservationId(), userId, paymentRequest.getPaymentMethod());
        
        // Pobierz rezerwację z PESSIMISTIC LOCK (blokuje do końca transakcji)
        // Zapobiega race conditions - jeśli dwie osoby próbują zapłacić jednocześnie,
        // druga będzie musiała czekać aż pierwsza zakończy
        Reservation reservation = reservationRepository.findByIdWithLock(paymentRequest.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Rezerwacja o ID " + paymentRequest.getReservationId() + " nie istnieje"));
        
        // Sprawdź czy rezerwacja należy do użytkownika
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do płatności za tę rezerwację");
        }
        
        // Sprawdź czy rezerwacja oczekuje na płatność (ponownie, po locku)
        // To ważne, bo status mógł się zmienić między wywołaniami
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT && 
            reservation.getStatus() != ReservationStatus.PAYMENT_FAILED) {
            throw new IllegalArgumentException(
                    "Rezerwacja nie oczekuje na płatność. Status: " + reservation.getStatus());
        }
        
        // SPECJALNA OBSŁUGA PŁATNOŚCI GOTÓWKĄ - automatyczne oznaczenie jako PAID
        if (paymentRequest.getPaymentMethod() == PaymentMethod.CASH) {
            // Płatność gotówką - natychmiastowe oznaczenie jako opłacone (bez opóźnienia, bez losowego sukcesu/błędu)
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
        
        // Dla innych metod płatności - normalna walidacja i symulacja
        // Waliduj dane płatności PRZED rozpoczęciem transakcji (oszczędność zasobów)
        validatePaymentData(paymentRequest);
        
        // Symuluj opóźnienie przetwarzania (1-3 sekundy)
        // UWAGA: To trzyma transakcję otwartą - w produkcji lepiej użyć async processing
        simulateProcessingDelay();
        
        // Losowo zdecyduj o sukcesie (90% sukcesu)
        boolean paymentSuccess = random.nextDouble() < 0.9;
        
        if (paymentSuccess) {
            // Płatność zakończona sukcesem
            String transactionId = generateTransactionId();
            BigDecimal amount = reservation.getTotalPrice();
            
            // Aktualizuj rezerwację
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaymentMethod(paymentRequest.getPaymentMethod());
            reservation.setPaymentDate(LocalDateTime.now());
            reservation.setPaymentTransactionId(transactionId);
            reservationRepository.save(reservation);
            
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
            // Płatność nie powiodła się
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
    
    /**
     * Waliduje format danych płatności (nie sprawdza rzeczywistych danych)
     */
    private void validatePaymentData(PaymentRequestDTO request) {
        PaymentMethod method = request.getPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                // Waliduj numer karty (tylko format: 13-19 cyfr)
                if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
                    throw new IllegalArgumentException("Numer karty jest wymagany");
                }
                String cardNumber = request.getCardNumber().replaceAll("\\s", "");
                if (!cardNumber.matches("\\d{13,19}")) {
                    throw new IllegalArgumentException("Numer karty musi zawierać 13-19 cyfr");
                }
                
                // Waliduj datę ważności (format: MM/RR)
                if (request.getExpiryDate() == null || !request.getExpiryDate().matches("\\d{2}/\\d{2}")) {
                    throw new IllegalArgumentException("Data ważności musi być w formacie MM/RR");
                }
                
                // Waliduj CVV (3-4 cyfry)
                if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
                    throw new IllegalArgumentException("CVV musi zawierać 3-4 cyfry");
                }
                break;
                
            case BLIK:
                // Waliduj kod BLIK (6 cyfr)
                if (request.getBlikCode() == null || !request.getBlikCode().matches("\\d{6}")) {
                    throw new IllegalArgumentException("Kod BLIK musi zawierać 6 cyfr");
                }
                break;
                
            case PAYPAL:
                // Waliduj email PayPal (podstawowa walidacja)
                if (request.getPaypalEmail() == null || !request.getPaypalEmail().contains("@")) {
                    throw new IllegalArgumentException("Nieprawidłowy adres email PayPal");
                }
                break;
                
            case CASH:
                // Gotówka - brak walidacji danych (płatność w kasie)
                break;
                
            case MOCK:
                // Symulacja - brak walidacji
                break;
                
            default:
                throw new IllegalArgumentException("Nieobsługiwana metoda płatności: " + method);
        }
    }
    
    /**
     * Symuluje opóźnienie przetwarzania płatności (1-3 sekundy)
     */
    private void simulateProcessingDelay() {
        try {
            int delay = 1000 + random.nextInt(2000); // 1-3 sekundy
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Przerwano symulację opóźnienia płatności");
        }
    }
    
    /**
     * Generuje unikalne ID transakcji
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }
}

