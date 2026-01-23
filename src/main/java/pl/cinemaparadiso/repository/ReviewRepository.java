package pl.cinemaparadiso.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Review;

import java.util.Optional;

/**
 * Repository dla encji Review
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Pobiera recenzje filmu posortowane od najnowszych
     */
    Page<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);
    
    /**
     * Znajduje recenzję użytkownika dla danego filmu
     */
    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);
    
    /**
     * Sprawdza czy użytkownik już napisał recenzję filmu
     */
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    
    /**
     * Liczy recenzje filmu
     */
    long countByMovieId(Long movieId);
    
    /**
     * Pobiera recenzje użytkownika posortowane od najnowszych
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Liczy recenzje użytkownika
     */
    long countByUserId(Long userId);
    
    /**
     * Liczy wszystkie recenzje w systemie
     */
    long count();
}

