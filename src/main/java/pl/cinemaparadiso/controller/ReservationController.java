package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.CreateReservationDTO;
import pl.cinemaparadiso.dto.ReservationDTO;
import pl.cinemaparadiso.dto.SeatAvailabilityDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.service.ReservationService;

import java.util.List;

/**
 * Controller dla endpointów związanych z rezerwacjami
 * 
 * @RestController - oznacza, że to jest REST Controller (zwraca JSON)
 * @RequestMapping - prefiks dla wszystkich endpointów w tym kontrolerze
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 * @Slf4j - Lombok generuje logger
 */
@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * Pobiera wszystkie rezerwacje zalogowanego użytkownika
     * 
     * GET /api/reservations/my
     * Wymaga zalogowania
     * 
     * @return lista rezerwacji użytkownika
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationDTO>> getMyReservations() {
        Long userId = getCurrentUserId();
        log.debug("Pobieranie rezerwacji użytkownika ID: {}", userId);
        
        List<ReservationDTO> reservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Tworzy nową rezerwację
     * 
     * POST /api/reservations
     * Wymaga zalogowania
     * 
     * @param createDTO - dane rezerwacji (walidowane przez @Valid)
     * @return utworzona rezerwacja
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody CreateReservationDTO createDTO) {
        Long userId = getCurrentUserId();
        log.info("Tworzenie rezerwacji przez użytkownika ID: {}", userId);
        
        ReservationDTO reservation = reservationService.createReservation(createDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }
    
    /**
     * Anuluje rezerwację (tylko własną)
     * 
     * DELETE /api/reservations/{id}
     * Wymaga zalogowania
     * 
     * @param id - ID rezerwacji do anulowania
     * @return 204 No Content jeśli sukces
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Anulowanie rezerwacji ID: {} przez użytkownika ID: {}", id, userId);
        
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Pobiera dostępne miejsca dla danego seansu
     * 
     * GET /api/screenings/{screeningId}/seats
     * Publiczny endpoint (nie wymaga logowania)
     * 
     * @param screeningId - ID seansu
     * @return lista dostępnych miejsc
     */
    @GetMapping("/screenings/{screeningId}/seats")
    public ResponseEntity<List<SeatAvailabilityDTO>> getAvailableSeats(@PathVariable Long screeningId) {
        log.debug("Pobieranie dostępnych miejsc dla seansu ID: {}", screeningId);
        
        List<SeatAvailabilityDTO> seats = reservationService.getAvailableSeats(screeningId);
        return ResponseEntity.ok(seats);
    }
    
    /**
     * Pobiera ID zalogowanego użytkownika z SecurityContext
     * 
     * @return ID użytkownika
     * @throws IllegalStateException jeśli użytkownik nie jest zalogowany
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Użytkownik nie jest zalogowany");
        }
        
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}


