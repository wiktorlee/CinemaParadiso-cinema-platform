package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.ScreeningSchedule;

import java.util.List;

/**
 * Repository dla encji ScreeningSchedule (Harmonogram Seansów)
 */
@Repository
public interface ScreeningScheduleRepository extends JpaRepository<ScreeningSchedule, Long> {
    
    /**
     * Znajduje wszystkie harmonogramy dla danego filmu
     */
    List<ScreeningSchedule> findByMovieId(Long movieId);
    
    /**
     * Znajduje wszystkie harmonogramy w danej sali
     */
    List<ScreeningSchedule> findByRoomId(Long roomId);
    
    /**
     * Znajduje harmonogramy dla danego filmu i sali
     */
    List<ScreeningSchedule> findByMovieIdAndRoomId(Long movieId, Long roomId);
}

