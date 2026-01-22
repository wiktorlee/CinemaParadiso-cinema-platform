package pl.cinemaparadiso.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Seat;

import java.util.List;
import java.util.Optional;

/**
 * Repository dla encji Seat (Miejsce)
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    /**
     * Znajduje wszystkie miejsca w danej sali
     */
    List<Seat> findByRoomId(Long roomId);
    
    /**
     * Znajduje wszystkie miejsca w danej sali, posortowane po rzędzie i numerze miejsca
     */
    List<Seat> findByRoomIdOrderByRowNumberAscSeatNumberAsc(Long roomId);
    
    /**
     * Sprawdza czy miejsce o podanym rzędzie i numerze już istnieje w sali
     */
    boolean existsByRoomIdAndRowNumberAndSeatNumber(Long roomId, Integer rowNumber, Integer seatNumber);
    
    /**
     * Pobiera miejsce z pessimistic lock (dla bezpiecznej rezerwacji)
     * Używa PESSIMISTIC_WRITE - blokuje miejsce do końca transakcji
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
}

