package pl.cinemaparadiso.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.AuditLogDTO;
import pl.cinemaparadiso.entity.audit.CustomRevisionEntity;
import pl.cinemaparadiso.repository.CustomRevisionEntityRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis do zarządzania logami audytowymi
 * Używa Hibernate Envers do pobierania historii zmian
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {
    
    private final EntityManager entityManager;
    private final CustomRevisionEntityRepository revisionRepository;
    
    /**
     * Pobiera logi audytowe z paginacją
     * Używa bezpośrednich zapytań SQL do tabel audytowych, aby uniknąć lazy loading
     */
    public Page<AuditLogDTO> getAuditLogs(Pageable pageable) {
        log.debug("Pobieranie logów audytowych: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        List<AuditLogDTO> logs = new ArrayList<>();
        
        // Pobierz rewizje z paginacją (sortowanie przez Pageable)
        Page<CustomRevisionEntity> revisionsPage = revisionRepository.findAll(pageable);
        
        // Dla każdej rewizji sprawdź jakie encje zostały zmienione używając native queries
        for (CustomRevisionEntity revision : revisionsPage.getContent()) {
            Long rev = revision.getRev();
            
            // Sprawdź każdy typ encji używając native queries (bez lazy loading)
            addAuditLogsForEntity("movies", "movies_aud", "Film", rev, revision, logs);
            addAuditLogsForEntity("rooms", "rooms_aud", "Sala", rev, revision, logs);
            addAuditLogsForEntity("screenings", "screenings_aud", "Seans", rev, revision, logs);
            addAuditLogsForEntity("reservations", "reservations_aud", "Rezerwacja", rev, revision, logs);
            addAuditLogsForEntity("users", "users_aud", "Użytkownik", rev, revision, logs);
        }
        
        // Sortuj po czasie (najnowsze pierwsze)
        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        // Zwróć z paginacją
        return new PageImpl<>(logs, pageable, logs.size());
    }
    
    /**
     * Dodaje logi audytowe dla konkretnego typu encji używając native query
     */
    private void addAuditLogsForEntity(String tableName, String auditTableName, String entityTypeName, 
                                       Long rev, CustomRevisionEntity revision, List<AuditLogDTO> logs) {
        try {
            // Pobierz zmiany dla tego typu encji w tej rewizji
            // Używamy native query aby uniknąć lazy loading
            // Jeśli tabela nie istnieje, zapytanie rzuci wyjątek, który zostanie przechwycony
            String query = String.format(
                "SELECT id, revtype FROM %s WHERE rev = :rev",
                auditTableName
            );
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(query)
                    .setParameter("rev", rev)
                    .getResultList();
            
            for (Object[] row : results) {
                Long entityId = ((Number) row[0]).longValue();
                Integer revtype = ((Number) row[1]).intValue();
                
                // Konwertuj revtype na RevisionType
                RevisionType revisionType = switch (revtype) {
                    case 0 -> RevisionType.ADD;
                    case 1 -> RevisionType.MOD;
                    case 2 -> RevisionType.DEL;
                    default -> RevisionType.MOD;
                };
                
                // Pobierz nazwę encji używając prostego zapytania (bez relacji)
                String entityName = getEntityNameFromTable(tableName, entityId);
                
                AuditLogDTO logDTO = createAuditLogDTOFromData(
                    entityTypeName, entityName, entityId, revision, revisionType
                );
                if (logDTO != null) {
                    logs.add(logDTO);
                }
            }
        } catch (Exception e) {
            // Jeśli nie ma zmian dla tego typu encji w tej rewizji lub tabela nie istnieje, kontynuuj
            log.trace("Brak zmian dla {} w rewizji {}: {}", tableName, rev, e.getMessage());
        }
    }
    
    /**
     * Pobiera nazwę encji bezpośrednio z tabeli (bez relacji)
     */
    private String getEntityNameFromTable(String tableName, Long entityId) {
        try {
            String query;
            switch (tableName) {
                case "movies":
                    query = "SELECT title FROM movies WHERE id = :id";
                    break;
                case "rooms":
                    query = "SELECT room_number FROM rooms WHERE id = :id";
                    break;
                case "screenings":
                    query = "SELECT start_time FROM screenings WHERE id = :id";
                    @SuppressWarnings("unchecked")
                    List<Object> timeResults = entityManager.createNativeQuery(query)
                            .setParameter("id", entityId)
                            .getResultList();
                    if (!timeResults.isEmpty()) {
                        return "Seans #" + entityId + " (" + timeResults.get(0) + ")";
                    }
                    return "Seans #" + entityId;
                case "reservations":
                    return "Rezerwacja #" + entityId;
                case "users":
                    query = "SELECT username FROM users WHERE id = :id";
                    break;
                default:
                    return tableName + " #" + entityId;
            }
            
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(query)
                    .setParameter("id", entityId)
                    .getResultList();
            
            if (!results.isEmpty() && results.get(0) != null) {
                return results.get(0).toString();
            }
        } catch (Exception e) {
            log.warn("Błąd podczas pobierania nazwy encji z tabeli {}: {}", tableName, e.getMessage());
        }
        return tableName + " #" + entityId;
    }
    
    /**
     * Tworzy DTO z danych (bez pobierania pełnej encji)
     */
    private AuditLogDTO createAuditLogDTOFromData(String entityType, String entityName, Long entityId,
                                                  CustomRevisionEntity revision, RevisionType revisionType) {
        try {
            // Konwertuj timestamp na LocalDateTime
            LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(revision.getRevtstmp()),
                    ZoneId.systemDefault()
            );
            
            // Konwertuj typ rewizji na czytelny tekst
            String revisionTypeStr = switch (revisionType) {
                case ADD -> "Dodano";
                case MOD -> "Zmodyfikowano";
                case DEL -> "Usunięto";
            };
            
            return AuditLogDTO.builder()
                    .revisionId(revision.getRev())
                    .timestamp(timestamp)
                    .userId(revision.getUserId())
                    .username(revision.getUserUsername() != null ? revision.getUserUsername() : "SYSTEM")
                    .ipAddress(revision.getIpAddress())
                    .entityType(entityType)
                    .entityId(entityId)
                    .revisionType(revisionTypeStr)
                    .entityName(entityName)
                    .changes("")
                    .build();
        } catch (Exception e) {
            log.warn("Błąd podczas tworzenia DTO dla logu audytowego: {}", e.getMessage());
            return null;
        }
    }
    
}

