package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Seat;

import java.util.List;

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
}

