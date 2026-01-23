package pl.cinemaparadiso.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Reservation;

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
}


