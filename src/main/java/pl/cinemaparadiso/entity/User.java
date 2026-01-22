package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.cinemaparadiso.enums.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * Encja reprezentująca użytkownika w systemie
 * 
 * @Entity - oznacza, że to jest encja JPA (tabela w bazie danych)
 * @Table(name = "users") - nazwa tabeli w bazie danych
 * 
 * UserDetails - interfejs Spring Security wymagany do autentykacji
 * 
 * Lombok:
 * @Data - automatycznie generuje gettery, settery, toString, equals, hashCode
 * @Builder - wzorzec Builder do łatwego tworzenia obiektów
 * @NoArgsConstructor - konstruktor bez argumentów (wymagany przez JPA)
 * @AllArgsConstructor - konstruktor ze wszystkimi argumentami
 */
@Entity
@Table(name = "users")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /**
     * @Id - pole klucza głównego
     * @GeneratedValue - automatyczne generowanie wartości (auto-increment)
     * strategy = GenerationType.IDENTITY - używa auto-increment z bazy danych
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column(unique = true, nullable = false)
     * unique = true - username musi być unikalny
     * nullable = false - username jest wymagany (NOT NULL w bazie)
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Hasło będzie przechowywane jako hash (BCrypt)
     * NIGDY nie przechowujemy haseł w plain text!
     */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /**
     * @Enumerated(EnumType.STRING) - przechowuje enum jako string w bazie
     * Przykład: "USER" zamiast 0, "ADMIN" zamiast 1
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Metody wymagane przez interfejs UserDetails (Spring Security)
     * Spring Security używa tych metod do autentykacji i autoryzacji
     */
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Zwraca listę uprawnień użytkownika
        // Spring Security używa tego do sprawdzania czy użytkownik może wykonać akcję
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security używa username do autentykacji
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Możemy dodać logikę wygasania konta w przyszłości
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Możemy dodać logikę blokowania konta w przyszłości
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Możemy dodać logikę wygasania haseł w przyszłości
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Możemy dodać logikę aktywacji konta w przyszłości
        return true;
    }
}

