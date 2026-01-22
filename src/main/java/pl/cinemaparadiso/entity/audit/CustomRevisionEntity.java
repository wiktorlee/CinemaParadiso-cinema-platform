package pl.cinemaparadiso.entity.audit;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import pl.cinemaparadiso.entity.audit.listener.AuditRevisionListener;

/**
 * Encja przechowująca informacje o każdej rewizji (zmianie) w systemie
 * 
 * Hibernate Envers automatycznie tworzy rekord w tej tabeli przy każdej zmianie
 * w audytowanych encjach.
 * 
 * @RevisionEntity - mówi Envers, że to jest encja rewizji
 * @RevisionListener - klasa, która automatycznie ustawia informacje o użytkowniku
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@Data
public class CustomRevisionEntity {

    /**
     * @RevisionNumber - numer rewizji (auto-increment)
     * To jest klucz główny tabeli REVINFO
     */
    @Id
    @RevisionNumber
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rev;

    /**
     * @RevisionTimestamp - timestamp zmiany (w milisekundach od 1970-01-01)
     * Automatycznie ustawiany przez Envers jako Long
     * Możemy konwertować na LocalDateTime w getterze jeśli potrzebne
     */
    @RevisionTimestamp
    @Column(nullable = false)
    private Long revtstmp;

    /**
     * ID użytkownika, który dokonał zmiany
     * Ustawiany automatycznie przez AuditRevisionListener
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Username użytkownika, który dokonał zmiany
     * Ustawiany automatycznie przez AuditRevisionListener
     */
    @Column(name = "user_username")
    private String userUsername;

    /**
     * IP adres użytkownika, który dokonał zmiany
     * Ustawiany automatycznie przez AuditRevisionListener
     */
    @Column(name = "ip_address")
    private String ipAddress;
}

