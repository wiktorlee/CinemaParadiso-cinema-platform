package pl.cinemaparadiso.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.AuditLogDTO;
import pl.cinemaparadiso.service.AuditService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller dla endpointów związanych z logami audytowymi
 * Tylko ADMIN może przeglądać logi
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    
    private final AuditService auditService;
    
    /**
     * Pobiera logi audytowe z paginacją
     * 
     * GET /api/admin/audit/logs?page=0&size=50
     * Tylko ADMIN
     * 
     * @param page - numer strony (domyślnie 0)
     * @param size - rozmiar strony (domyślnie 50)
     * @return strona z logami audytowymi
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.debug("Pobieranie logów audytowych: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "revtstmp"));
        Page<AuditLogDTO> logsPage = auditService.getAuditLogs(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", logsPage.getContent());
        response.put("totalElements", logsPage.getTotalElements());
        response.put("totalPages", logsPage.getTotalPages());
        response.put("currentPage", logsPage.getNumber());
        response.put("pageSize", logsPage.getSize());
        response.put("hasNext", logsPage.hasNext());
        response.put("hasPrevious", logsPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
}

