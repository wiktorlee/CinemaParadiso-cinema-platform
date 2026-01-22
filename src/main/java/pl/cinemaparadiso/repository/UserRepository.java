package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.User;

import java.util.Optional;

/**
 * Repository dla encji User
 * 
 * Spring Data JPA automatycznie tworzy implementację tego interfejsu
 * Wystarczy zdefiniować metody, a Spring utworzy zapytania SQL
 * 
 * @Repository - oznacza, że to jest komponent Spring (warstwa dostępu do danych)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Znajduje użytkownika po username
     * Spring Data JPA automatycznie tworzy zapytanie:
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username - username użytkownika
     * @return Optional<User> - użytkownik lub pusty Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Sprawdza czy użytkownik o danym username istnieje
     * Spring Data JPA automatycznie tworzy zapytanie:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     * 
     * @param username - username do sprawdzenia
     * @return true jeśli istnieje, false jeśli nie
     */
    boolean existsByUsername(String username);
}



