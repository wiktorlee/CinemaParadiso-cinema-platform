package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.CreateScreeningScheduleDTO;
import pl.cinemaparadiso.dto.ScreeningDTO;
import pl.cinemaparadiso.dto.ScreeningScheduleDTO;
import pl.cinemaparadiso.dto.UpdateScreeningScheduleDTO;
import pl.cinemaparadiso.service.ScreeningScheduleService;

import java.util.List;

/**
 * Controller dla endpointów związanych z harmonogramami seansów
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScreeningScheduleController {
    
    private final ScreeningScheduleService scheduleService;
    
    /**
     * Pobiera wszystkie harmonogramy
     * 
     * GET /api/schedules
     */
    @GetMapping
    public ResponseEntity<List<ScreeningScheduleDTO>> getAllSchedules() {
        log.info("GET /api/schedules - pobieranie wszystkich harmonogramów");
        List<ScreeningScheduleDTO> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * Pobiera harmonogram po ID
     * 
     * GET /api/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningScheduleDTO> getScheduleById(@PathVariable Long id) {
        log.info("GET /api/schedules/{} - pobieranie harmonogramu", id);
        ScreeningScheduleDTO schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }
    
    /**
     * Tworzy nowy harmonogram
     * 
     * POST /api/schedules
     * Tylko ADMIN może tworzyć harmonogramy
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningScheduleDTO> createSchedule(@Valid @RequestBody CreateScreeningScheduleDTO dto) {
        log.info("POST /api/schedules - tworzenie harmonogramu");
        ScreeningScheduleDTO schedule = scheduleService.createSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }
    
    /**
     * Aktualizuje harmonogram
     * 
     * PUT /api/schedules/{id}
     * Tylko ADMIN może aktualizować harmonogramy
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningScheduleDTO> updateSchedule(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateScreeningScheduleDTO dto) {
        log.info("PUT /api/schedules/{} - aktualizacja harmonogramu", id);
        ScreeningScheduleDTO schedule = scheduleService.updateSchedule(id, dto);
        return ResponseEntity.ok(schedule);
    }
    
    /**
     * Usuwa harmonogram
     * 
     * DELETE /api/schedules/{id}
     * Tylko ADMIN może usuwać harmonogramy
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        log.info("DELETE /api/schedules/{} - usuwanie harmonogramu", id);
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Generuje seanse z harmonogramu
     * 
     * POST /api/schedules/{id}/generate
     * Tylko ADMIN może generować seanse
     */
    @PostMapping("/{id}/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScreeningDTO>> generateScreenings(@PathVariable Long id) {
        log.info("POST /api/schedules/{}/generate - generowanie seansów z harmonogramu", id);
        List<ScreeningDTO> screenings = scheduleService.generateScreeningsFromSchedule(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(screenings);
    }
}

