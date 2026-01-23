package pl.cinemaparadiso.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cinemaparadiso.service.StatisticsService;

import java.util.Map;

/**
 * Controller dla statystyk panelu admina
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Pobiera wszystkie statystyki
     * 
     * GET /api/admin/statistics
     * Tylko ADMIN
     * 
     * @return map ze wszystkimi statystykami
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStatistics() {
        log.debug("Pobieranie statystyk przez admina");
        Map<String, Object> statistics = statisticsService.getAllStatistics();
        return ResponseEntity.ok(statistics);
    }
}

