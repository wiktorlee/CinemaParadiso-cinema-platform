package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO dla logów audytowych
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    
    private Long revisionId;
    private LocalDateTime timestamp;
    private Long userId;
    private String username;
    private String ipAddress;
    private String entityType; // Typ encji (np. "Movie", "Reservation")
    private Long entityId; // ID zmienionej encji
    private String revisionType; // ADD, MOD, DEL
    private String entityName; // Nazwa encji (np. tytuł filmu, numer sali)
    private String changes; // Opis zmian (JSON lub tekst)
}


