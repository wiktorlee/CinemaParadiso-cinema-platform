package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.CreateScreeningDTO;
import pl.cinemaparadiso.dto.ScreeningDTO;
import pl.cinemaparadiso.dto.UpdateScreeningDTO;
import pl.cinemaparadiso.service.ScreeningService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller dla endpointów związanych z seansami
 */
@Slf4j
@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
public class ScreeningController {
    
    private final ScreeningService screeningService;
    
    /**
     * Pobiera wszystkie seanse (z paginacją)
     * 
     * GET /api/screenings?page=0&size=20&sort=startTime,asc
     */
    @GetMapping
    public ResponseEntity<?> getAllScreenings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "startTime,asc") String sort) {
        
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String[] sortParams = sort.split(",");
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortParams[0]);
        
        Page<ScreeningDTO> screenings = screeningService.getAllScreenings(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", screenings.getContent());
        response.put("totalElements", screenings.getTotalElements());
        response.put("totalPages", screenings.getTotalPages());
        response.put("currentPage", screenings.getNumber());
        response.put("pageSize", screenings.getSize());
        response.put("hasNext", screenings.hasNext());
        response.put("hasPrevious", screenings.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pobiera nadchodzące seanse (startTime >= teraz)
     * 
     * GET /api/screenings/upcoming?page=0&size=20
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingScreenings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "startTime,asc") String sort) {
        
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String[] sortParams = sort.split(",");
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortParams[0]);
        
        Page<ScreeningDTO> screenings = screeningService.getUpcomingScreenings(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", screenings.getContent());
        response.put("totalElements", screenings.getTotalElements());
        response.put("totalPages", screenings.getTotalPages());
        response.put("currentPage", screenings.getNumber());
        response.put("pageSize", screenings.getSize());
        response.put("hasNext", screenings.hasNext());
        response.put("hasPrevious", screenings.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pobiera seanse w zakresie dat
     * 
     * GET /api/screenings/range?start=2024-01-15T00:00&end=2024-01-20T23:59
     */
    @GetMapping("/range")
    public ResponseEntity<?> getScreeningsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "startTime,asc") String sort) {
        
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String[] sortParams = sort.split(",");
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortParams[0]);
        
        Page<ScreeningDTO> screenings = screeningService.getScreeningsByDateRange(start, end, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", screenings.getContent());
        response.put("totalElements", screenings.getTotalElements());
        response.put("totalPages", screenings.getTotalPages());
        response.put("currentPage", screenings.getNumber());
        response.put("pageSize", screenings.getSize());
        response.put("hasNext", screenings.hasNext());
        response.put("hasPrevious", screenings.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pobiera seanse dla danego filmu
     * 
     * GET /api/screenings/movie/{movieId}?page=0&size=20
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getScreeningsByMovie(
            @PathVariable Long movieId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "startTime,asc") String sort) {
        
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String[] sortParams = sort.split(",");
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortDirection, sortParams[0]);
        
        Page<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(movieId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", screenings.getContent());
        response.put("totalElements", screenings.getTotalElements());
        response.put("totalPages", screenings.getTotalPages());
        response.put("currentPage", screenings.getNumber());
        response.put("pageSize", screenings.getSize());
        response.put("hasNext", screenings.hasNext());
        response.put("hasPrevious", screenings.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pobiera seans po ID
     * 
     * GET /api/screenings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDTO> getScreeningById(@PathVariable Long id) {
        log.info("GET /api/screenings/{} - pobieranie seansu", id);
        ScreeningDTO screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }
    
    /**
     * Tworzy nowy seans
     * 
     * POST /api/screenings
     * Tylko ADMIN może tworzyć seanse
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> createScreening(@Valid @RequestBody CreateScreeningDTO dto) {
        log.info("POST /api/screenings - tworzenie seansu");
        ScreeningDTO screening = screeningService.createScreening(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(screening);
    }
    
    /**
     * Aktualizuje seans
     * 
     * PUT /api/screenings/{id}
     * Tylko ADMIN może aktualizować seanse
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> updateScreening(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateScreeningDTO dto) {
        log.info("PUT /api/screenings/{} - aktualizacja seansu", id);
        ScreeningDTO screening = screeningService.updateScreening(id, dto);
        return ResponseEntity.ok(screening);
    }
    
    /**
     * Usuwa seans
     * 
     * DELETE /api/screenings/{id}
     * Tylko ADMIN może usuwać seanse
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        log.info("DELETE /api/screenings/{} - usuwanie seansu", id);
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }
}

