package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.ReservationSeat;

import java.util.List;

/**
 * Repository dla encji ReservationSeat (Miejsce w Rezerwacji)
 */
@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    
    /**
     * Znajduje wszystkie zarezerwowane miejsca dla danego seansu
     * Uwzględnia rezerwacje: PAID (opłacone) i PENDING_PAYMENT (oczekujące na płatność)
     * Nie uwzględnia: CANCELLED, PAYMENT_FAILED
     */
    @Query("SELECT rs FROM ReservationSeat rs " +
           "WHERE rs.reservation.screening.id = :screeningId " +
           "AND rs.reservation.status IN ('PAID', 'PENDING_PAYMENT')")
    List<ReservationSeat> findReservedSeatsByScreeningId(@Param("screeningId") Long screeningId);
    
    /**
     * Sprawdza czy miejsce jest już zarezerwowane na dany seans
     * Uwzględnia rezerwacje: PAID (opłacone) i PENDING_PAYMENT (oczekujące na płatność)
     * Nie uwzględnia: CANCELLED, PAYMENT_FAILED
     */
    @Query("SELECT COUNT(rs) > 0 FROM ReservationSeat rs " +
           "WHERE rs.seat.id = :seatId " +
           "AND rs.reservation.screening.id = :screeningId " +
           "AND rs.reservation.status IN ('PAID', 'PENDING_PAYMENT')")
    boolean isSeatReservedForScreening(@Param("seatId") Long seatId, @Param("screeningId") Long screeningId);
    
    /**
     * Sprawdza czy miejsce jest zarezerwowane przez INNĄ rezerwację (nie podaną)
     * Używane do weryfikacji przed finalizacją płatności
     * Uwzględnia rezerwacje: PAID (opłacone) i PENDING_PAYMENT (oczekujące na płatność)
     */
    @Query("SELECT COUNT(rs) > 0 FROM ReservationSeat rs " +
           "WHERE rs.seat.id = :seatId " +
           "AND rs.reservation.screening.id = :screeningId " +
           "AND rs.reservation.id != :excludeReservationId " +
           "AND rs.reservation.status IN ('PAID', 'PENDING_PAYMENT')")
    boolean isSeatReservedByOtherReservation(@Param("seatId") Long seatId, 
                                             @Param("screeningId") Long screeningId,
                                             @Param("excludeReservationId") Long excludeReservationId);
}


