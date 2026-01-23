package pl.cinemaparadiso.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Reservation (Rezerwacja)
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * Znajduje wszystkie rezerwacje użytkownika
     */
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Znajduje wszystkie opłacone rezerwacje dla danego seansu
     */
    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId AND r.status = 'PAID'")
    List<Reservation> findPaidReservationsByScreeningId(@Param("screeningId") Long screeningId);
    
    /**
     * Sprawdza czy użytkownik ma rezerwację o danym ID
     */
    boolean existsByIdAndUserId(Long reservationId, Long userId);
    
    /**
     * Pobiera rezerwację z pessimistic lock (blokuje do końca transakcji)
     * Używane do zapobiegania race conditions podczas płatności
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);
    
    /**
     * Pobiera wszystkie opłacone rezerwacje z załadowanymi miejscami (dla statystyk)
     * JOIN FETCH zapewnia, że reservationSeats są załadowane (nie lazy loading)
     */
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "LEFT JOIN FETCH r.reservationSeats " +
           "WHERE r.status = 'PAID'")
    List<Reservation> findAllPaidReservationsWithSeats();
    
    /**
     * Pobiera opłacone rezerwacje z datą płatności w zakresie (z załadowanymi miejscami)
     * Uwzględnia rezerwacje z paymentDate w zakresie LUB z createdAt w zakresie (dla starych rezerwacji bez paymentDate)
     */
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "LEFT JOIN FETCH r.reservationSeats " +
           "WHERE r.status = 'PAID' " +
           "AND ((r.paymentDate >= :startDate AND r.paymentDate < :endDate) " +
           "     OR (r.paymentDate IS NULL AND r.createdAt >= :startDate AND r.createdAt < :endDate))")
    List<Reservation> findPaidReservationsByPaymentDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}


