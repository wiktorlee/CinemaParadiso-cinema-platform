package pl.cinemaparadiso.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Screening;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository dla encji Screening (Seans)
 */
@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    
    /**
     * Znajduje wszystkie seanse dla danego filmu
     */
    List<Screening> findByMovieId(Long movieId);
    
    /**
     * Znajduje wszystkie seanse w danej sali
     */
    List<Screening> findByRoomId(Long roomId);
    
    /**
     * Znajduje wszystkie seanse dla danego filmu i sali
     */
    List<Screening> findByMovieIdAndRoomId(Long movieId, Long roomId);
    
    /**
     * Znajduje seanse w danej sali w zakresie dat (dla sprawdzania kolizji i wyświetlania)
     */
    List<Screening> findByRoomIdAndStartTimeBetween(Long roomId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Znajduje seanse w danym zakresie dat (dla wyświetlania)
     */
    Page<Screening> findByStartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Znajduje seanse dla danego filmu w zakresie dat
     */
    Page<Screening> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Znajduje seanse w danej sali w zakresie dat (z paginacją, dla wyświetlania)
     */
    Page<Screening> findByRoomIdAndStartTimeBetween(Long roomId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Znajduje nadchodzące seanse (startTime >= teraz)
     */
    Page<Screening> findByStartTimeGreaterThanEqual(LocalDateTime now, Pageable pageable);
    
    /**
     * Znajduje nadchodzące seanse dla danego filmu
     */
    Page<Screening> findByMovieIdAndStartTimeGreaterThanEqual(Long movieId, LocalDateTime now, Pageable pageable);
}

