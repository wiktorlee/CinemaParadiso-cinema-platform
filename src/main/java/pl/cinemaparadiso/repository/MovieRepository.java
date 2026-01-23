package pl.cinemaparadiso.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Movie;

import java.util.List;

/**
 * Repository dla encji Movie
 * 
 * @Repository - oznacza, że to jest komponent Spring (warstwa dostępu do danych)
 * JpaRepository<Movie, Long> - Spring Data JPA automatycznie tworzy implementację
 * - Movie - typ encji
 * - Long - typ klucza głównego (ID)
 * 
 * Spring Data JPA automatycznie tworzy metody:
 * - findAll() - zwraca wszystkie filmy
 * - findById(Long id) - zwraca film po ID
 * - save(Movie movie) - zapisuje film
 * - deleteById(Long id) - usuwa film
 * - itd.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    /**
     * Wyszukuje filmy po tytule (case-insensitive, częściowe dopasowanie)
     * Spring Data JPA automatycznie tworzy implementację na podstawie nazwy metody
     * 
     * @param title - tytuł do wyszukania
     * @return lista filmów pasujących do tytułu
     */
    List<Movie> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Wyszukuje filmy po tytule z paginacją
     * 
     * @param title - tytuł do wyszukania
     * @param pageable - parametry paginacji
     * @return strona z filmami
     */
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Wyszukuje filmy po gatunku
     * 
     * @param genre - gatunek filmu
     * @return lista filmów danego gatunku
     */
    List<Movie> findByGenre(String genre);
    
    /**
     * Wyszukuje filmy po gatunku z paginacją
     * 
     * @param genre - gatunek filmu
     * @param pageable - parametry paginacji
     * @return strona z filmami
     */
    Page<Movie> findByGenre(String genre, Pageable pageable);
    
    /**
     * Sprawdza czy film o danym tytule już istnieje
     * 
     * @param title - tytuł filmu
     * @return true jeśli film istnieje, false w przeciwnym razie
     */
    boolean existsByTitle(String title);
    
    /**
     * Pobiera najnowsze premiery (sortowane po dacie premiery, najnowsze najpierw)
     * 
     * @param pageable - parametry paginacji
     * @return strona z filmami
     */
    Page<Movie> findByReleaseDateIsNotNullOrderByReleaseDateDesc(Pageable pageable);
    
    /**
     * Pobiera najnowsze premiery (bez paginacji, limit)
     * Używa Pageable z limitem
     * 
     * @param pageable - parametry paginacji (użyj PageRequest.of(0, limit))
     * @return lista filmów
     */
    @Query("SELECT m FROM Movie m WHERE m.releaseDate IS NOT NULL ORDER BY m.releaseDate DESC")
    List<Movie> findLatestReleases(Pageable pageable);
    
    /**
     * Pobiera najpopularniejsze filmy (sortowane po średniej ocenie, najwyższe najpierw)
     * Uwzględnia tylko filmy, które mają co najmniej jedną ocenę
     * 
     * @param pageable - parametry paginacji (użyj PageRequest.of(0, limit))
     * @return lista filmów
     */
    @Query("SELECT m FROM Movie m " +
           "LEFT JOIN MovieRating mr ON mr.movie.id = m.id " +
           "GROUP BY m.id " +
           "HAVING COUNT(mr.id) > 0 " +
           "ORDER BY AVG(mr.rating) DESC, COUNT(mr.id) DESC")
    List<Movie> findMostPopularMovies(Pageable pageable);
}

