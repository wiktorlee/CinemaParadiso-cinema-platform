package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.CreateMultipleRoomsDTO;
import pl.cinemaparadiso.dto.CreateRoomDTO;
import pl.cinemaparadiso.dto.DuplicateRoomDTO;
import pl.cinemaparadiso.dto.RoomDTO;
import pl.cinemaparadiso.dto.SeatDTO;
import pl.cinemaparadiso.dto.UpdateRoomDTO;
import pl.cinemaparadiso.service.RoomService;

import java.util.List;

/**
 * Controller dla endpointów związanych z salami kinowymi
 * 
 * @RestController - oznacza, że to jest REST Controller (zwraca JSON)
 * @RequestMapping - prefiks dla wszystkich endpointów w tym kontrolerze
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 * @Slf4j - Lombok generuje logger
 */
@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    /**
     * Pobiera wszystkie sale
     * 
     * GET /api/rooms
     * 
     * @return lista wszystkich sal
     */
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        log.info("GET /api/rooms - pobieranie wszystkich sal");
        List<RoomDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * Pobiera salę po ID
     * 
     * GET /api/rooms/{id}
     * 
     * @param id - ID sali
     * @return sala (404 jeśli nie znaleziono)
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        log.info("GET /api/rooms/{} - pobieranie sali", id);
        RoomDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }
    
    /**
     * Pobiera wszystkie miejsca w sali
     * 
     * GET /api/rooms/{id}/seats
     * 
     * @param id - ID sali
     * @return lista miejsc w sali (404 jeśli sala nie istnieje)
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoomId(@PathVariable Long id) {
        log.info("GET /api/rooms/{}/seats - pobieranie miejsc w sali", id);
        List<SeatDTO> seats = roomService.getSeatsByRoomId(id);
        return ResponseEntity.ok(seats);
    }
    
    /**
     * Tworzy nową salę (automatycznie generuje miejsca)
     * 
     * POST /api/rooms
     * Tylko ADMIN może tworzyć sale
     * 
     * @param dto - dane sali do utworzenia
     * @return utworzona sala
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody CreateRoomDTO dto) {
        log.info("POST /api/rooms - tworzenie sali: {}", dto.getRoomNumber());
        RoomDTO room = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
    
    /**
     * Aktualizuje istniejącą salę (tylko roomNumber i description)
     * 
     * PUT /api/rooms/{id}
     * Tylko ADMIN może aktualizować sale
     * 
     * @param id - ID sali do aktualizacji
     * @param dto - dane do aktualizacji
     * @return zaktualizowana sala (404/409 jeśli błąd)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @Valid @RequestBody UpdateRoomDTO dto) {
        log.info("PUT /api/rooms/{} - aktualizacja sali", id);
        RoomDTO room = roomService.updateRoom(id, dto);
        return ResponseEntity.ok(room);
    }
    
    /**
     * Usuwa salę
     * 
     * DELETE /api/rooms/{id}
     * Tylko ADMIN może usuwać sale
     * 
     * @param id - ID sali do usunięcia
     * @return 204 No Content jeśli sukces (404 jeśli nie znaleziono)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        log.info("DELETE /api/rooms/{} - usuwanie sali", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Duplikuje salę - tworzy nową salę z taką samą konfiguracją
     * 
     * POST /api/rooms/{id}/duplicate
     * Tylko ADMIN może duplikować sale
     * 
     * @param id - ID sali źródłowej
     * @param dto - dane nowej sali (numer)
     * @return utworzona sala
     */
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> duplicateRoom(@PathVariable Long id, @Valid @RequestBody DuplicateRoomDTO dto) {
        log.info("POST /api/rooms/{}/duplicate - duplikowanie sali jako: {}", id, dto.getNewRoomNumber());
        RoomDTO room = roomService.duplicateRoom(id, dto.getNewRoomNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
    
    /**
     * Tworzy wiele sal o tym samym formacie
     * 
     * POST /api/rooms/bulk
     * Tylko ADMIN może tworzyć wiele sal
     * 
     * @param dto - dane do masowego tworzenia sal
     * @return lista utworzonych sal
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomDTO>> createMultipleRooms(@Valid @RequestBody CreateMultipleRoomsDTO dto) {
        log.info("POST /api/rooms/bulk - tworzenie {} sal", dto.getCount());
        List<RoomDTO> rooms = roomService.createMultipleRooms(
                dto.getRoomNumberPrefix(),
                dto.getStartNumber(),
                dto.getCount(),
                dto.getTotalRows(),
                dto.getSeatsPerRow(),
                dto.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(rooms);
    }
}

