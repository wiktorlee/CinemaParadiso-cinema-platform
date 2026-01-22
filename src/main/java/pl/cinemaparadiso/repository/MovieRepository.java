package pl.cinemaparadiso.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Movie;

import java.util.List;
import java.util.Optional;

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
}

