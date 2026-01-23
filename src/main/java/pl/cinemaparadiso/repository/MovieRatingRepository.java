package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.MovieRating;

import java.util.Optional;

/**
 * Repository dla encji MovieRating
 */
@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, Long> {
    
    /**
     * Znajduje ocenę użytkownika dla danego filmu
     */
    Optional<MovieRating> findByUserIdAndMovieId(Long userId, Long movieId);
    
    /**
     * Sprawdza czy użytkownik już ocenił film
     */
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    
    /**
     * Oblicza średnią ocenę filmu
     */
    @Query("SELECT AVG(mr.rating) FROM MovieRating mr WHERE mr.movie.id = :movieId")
    Double getAverageRatingByMovieId(@Param("movieId") Long movieId);
    
    /**
     * Liczy liczbę ocen filmu
     */
    @Query("SELECT COUNT(mr) FROM MovieRating mr WHERE mr.movie.id = :movieId")
    Long getRatingCountByMovieId(@Param("movieId") Long movieId);
    
    /**
     * Liczy wszystkie oceny w systemie
     */
    long count();
}


