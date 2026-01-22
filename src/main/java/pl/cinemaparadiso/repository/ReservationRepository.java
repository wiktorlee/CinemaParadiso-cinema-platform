package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Reservation;

import java.util.List;

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
     * Znajduje wszystkie aktywne rezerwacje dla danego seansu
     */
    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId AND r.status = 'ACTIVE'")
    List<Reservation> findActiveReservationsByScreeningId(@Param("screeningId") Long screeningId);
    
    /**
     * Sprawdza czy użytkownik ma rezerwację o danym ID
     */
    boolean existsByIdAndUserId(Long reservationId, Long userId);
}


