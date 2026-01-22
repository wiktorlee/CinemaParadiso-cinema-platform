package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.CreateRoomDTO;
import pl.cinemaparadiso.dto.RoomDTO;
import pl.cinemaparadiso.dto.SeatDTO;
import pl.cinemaparadiso.dto.UpdateRoomDTO;
import pl.cinemaparadiso.entity.Room;
import pl.cinemaparadiso.entity.Seat;
import pl.cinemaparadiso.enums.SeatType;
import pl.cinemaparadiso.exception.RoomAlreadyExistsException;
import pl.cinemaparadiso.exception.RoomNotFoundException;
import pl.cinemaparadiso.repository.RoomRepository;
import pl.cinemaparadiso.repository.SeatRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania salami kinowymi
 * Zawiera logikę biznesową dla operacji CRUD na salach i automatyczne generowanie miejsc
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    
    /**
     * Konwertuje encję Room na DTO
     */
    private RoomDTO toDTO(Room room) {
        List<SeatDTO> seatsDTO = room.getSeats().stream()
                .map(this::seatToDTO)
                .collect(Collectors.toList());
        
        return RoomDTO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .totalRows(room.getTotalRows())
                .seatsPerRow(room.getSeatsPerRow())
                .description(room.getDescription())
                .capacity(room.getTotalRows() * room.getSeatsPerRow())
                .seats(seatsDTO)
                .build();
    }
    
    /**
     * Konwertuje encję Seat na DTO
     */
    private SeatDTO seatToDTO(Seat seat) {
        return SeatDTO.builder()
                .id(seat.getId())
                .roomId(seat.getRoom().getId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .isAvailable(seat.getIsAvailable())
                .build();
    }
    
    /**
     * Pobiera wszystkie sale
     */
    @Transactional(readOnly = true)
    public List<RoomDTO> getAllRooms() {
        log.info("Pobieranie wszystkich sal");
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Pobiera salę po ID
     */
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long id) {
        log.info("Pobieranie sali o ID: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        return toDTO(room);
    }
    
    /**
     * Tworzy nową salę i automatycznie generuje miejsca
     * Dwa środkowe rzędy są typu VIP, reszta STANDARD
     */
    public RoomDTO createRoom(CreateRoomDTO dto) {
        log.info("Tworzenie nowej sali: {}", dto.getRoomNumber());
        
        // Sprawdź czy sala o takim numerze już istnieje
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            log.warn("Próba utworzenia sali o istniejącym numerze: {}", dto.getRoomNumber());
            throw new RoomAlreadyExistsException("Sala o numerze '" + dto.getRoomNumber() + "' już istnieje");
        }
        
        // Utwórz nową salę
        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .totalRows(dto.getTotalRows())
                .seatsPerRow(dto.getSeatsPerRow())
                .description(dto.getDescription())
                .build();
        
        // Automatycznie generuj miejsca
        generateSeats(room);
        
        // Zapisz salę (miejsca zostaną zapisane automatycznie dzięki cascade)
        Room savedRoom = roomRepository.save(room);
        log.info("Sala utworzona pomyślnie: ID={}, numer={}, miejsc={}", 
                savedRoom.getId(), savedRoom.getRoomNumber(), savedRoom.getSeats().size());
        
        return toDTO(savedRoom);
    }
    
    /**
     * Automatycznie generuje miejsca dla sali
     * Dwa środkowe rzędy są typu VIP, reszta STANDARD
     * 
     * Przykłady:
     * - 10 rzędów: rzędy 5 i 6 = VIP
     * - 9 rzędów: rzędy 4 i 5 = VIP
     * - 8 rzędów: rzędy 4 i 5 = VIP
     */
    private void generateSeats(Room room) {
        int totalRows = room.getTotalRows();
        int seatsPerRow = room.getSeatsPerRow();
        
        // Oblicz numery rzędów VIP (dwa środkowe rzędy)
        int vipRow1 = totalRows / 2; // Zaokrąglone w dół
        int vipRow2 = vipRow1 + 1;
        
        log.debug("Generowanie miejsc dla sali: {} rzędów x {} miejsc, VIP rzędy: {} i {}", 
                totalRows, seatsPerRow, vipRow1, vipRow2);
        
        // Generuj miejsca dla każdego rzędu
        for (int row = 1; row <= totalRows; row++) {
            // Określ typ miejsca: VIP dla dwóch środkowych rzędów, STANDARD dla reszty
            SeatType seatType = (row == vipRow1 || row == vipRow2) ? SeatType.VIP : SeatType.STANDARD;
            
            // Generuj miejsca w rzędzie
            for (int seat = 1; seat <= seatsPerRow; seat++) {
                Seat newSeat = Seat.builder()
                        .room(room)
                        .rowNumber(row)
                        .seatNumber(seat)
                        .seatType(seatType)
                        .isAvailable(true)
                        .build();
                
                room.getSeats().add(newSeat);
            }
        }
        
        log.info("Wygenerowano {} miejsc dla sali {} (VIP rzędy: {} i {})", 
                room.getSeats().size(), room.getRoomNumber(), vipRow1, vipRow2);
    }
    
    /**
     * Aktualizuje salę (tylko roomNumber i description)
     * Liczba rzędów i miejsc jest niezmienna
     */
    public RoomDTO updateRoom(Long id, UpdateRoomDTO dto) {
        log.info("Aktualizacja sali o ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        
        // Sprawdź czy nowy numer sali nie koliduje z inną salą
        if (!room.getRoomNumber().equals(dto.getRoomNumber()) && 
            roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            log.warn("Próba zmiany numeru sali na istniejący: {}", dto.getRoomNumber());
            throw new RoomAlreadyExistsException("Sala o numerze '" + dto.getRoomNumber() + "' już istnieje");
        }
        
        // Aktualizuj tylko dozwolone pola
        room.setRoomNumber(dto.getRoomNumber());
        room.setDescription(dto.getDescription());
        
        Room updatedRoom = roomRepository.save(room);
        log.info("Sala zaktualizowana pomyślnie: {}", updatedRoom.getRoomNumber());
        
        return toDTO(updatedRoom);
    }
    
    /**
     * Usuwa salę (miejsca zostaną usunięte automatycznie dzięki cascade)
     * UWAGA: Nie można usunąć sali jeśli ma seanse lub rezerwacje!
     */
    public void deleteRoom(Long id) {
        log.info("Usuwanie sali o ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        
        // TODO: Sprawdź czy sala ma seanse/rezerwacje (będzie w następnym etapie)
        // if (!room.getScreenings().isEmpty()) {
        //     throw new RoomHasScreeningsException("Nie można usunąć sali która ma seanse");
        // }
        
        roomRepository.delete(room);
        log.info("Sala usunięta pomyślnie: {}", room.getRoomNumber());
    }
    
    /**
     * Pobiera wszystkie miejsca w sali, posortowane po rzędzie i numerze miejsca
     */
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatsByRoomId(Long roomId) {
        log.info("Pobieranie miejsc dla sali o ID: {}", roomId);
        
        // Sprawdź czy sala istnieje
        if (!roomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }
        
        return seatRepository.findByRoomIdOrderByRowNumberAscSeatNumberAsc(roomId).stream()
                .map(this::seatToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Duplikuje salę - tworzy nową salę z taką samą konfiguracją (liczba rzędów, miejsc)
     * ale z nowym numerem sali
     * 
     * @param sourceRoomId - ID sali źródłowej do skopiowania
     * @param newRoomNumber - numer nowej sali
     * @return utworzona sala
     */
    public RoomDTO duplicateRoom(Long sourceRoomId, String newRoomNumber) {
        log.info("Duplikowanie sali ID={} jako: {}", sourceRoomId, newRoomNumber);
        
        // Sprawdź czy sala źródłowa istnieje
        Room sourceRoom = roomRepository.findById(sourceRoomId)
                .orElseThrow(() -> new RoomNotFoundException(sourceRoomId));
        
        // Sprawdź czy nowy numer sali nie koliduje
        if (roomRepository.existsByRoomNumber(newRoomNumber)) {
            throw new RoomAlreadyExistsException("Sala o numerze '" + newRoomNumber + "' już istnieje");
        }
        
        // Utwórz nową salę z taką samą konfiguracją
        Room newRoom = Room.builder()
                .roomNumber(newRoomNumber)
                .totalRows(sourceRoom.getTotalRows())
                .seatsPerRow(sourceRoom.getSeatsPerRow())
                .description(sourceRoom.getDescription())
                .build();
        
        // Automatycznie generuj miejsca (ta sama logika VIP)
        generateSeats(newRoom);
        
        Room savedRoom = roomRepository.save(newRoom);
        log.info("Sala zduplikowana pomyślnie: ID={}, numer={}, miejsc={}", 
                savedRoom.getId(), savedRoom.getRoomNumber(), savedRoom.getSeats().size());
        
        return toDTO(savedRoom);
    }
    
    /**
     * Tworzy wiele sal o tym samym formacie
     * 
     * UWAGA: Jeśli któraś sala już istnieje, metoda rzuci wyjątek i cała transakcja zostanie wycofana (rollback).
     * To zapewnia spójność danych - albo wszystkie sale zostaną utworzone, albo żadna.
     * 
     * @param roomNumberPrefix - prefix numeru sali (np. "Sala")
     * @param startNumber - numer początkowy (np. 1)
     * @param count - liczba sal do utworzenia
     * @param totalRows - liczba rzędów
     * @param seatsPerRow - liczba miejsc w rzędzie
     * @param description - opcjonalny opis (będzie dodany do każdej sali)
     * @return lista utworzonych sal
     * @throws RoomAlreadyExistsException jeśli któraś sala już istnieje (cała operacja zostanie wycofana)
     */
    public List<RoomDTO> createMultipleRooms(String roomNumberPrefix, int startNumber, int count, 
                                               int totalRows, int seatsPerRow, String description) {
        log.info("Tworzenie {} sal: {} {}-{} ({} rzędów x {} miejsc)", 
                count, roomNumberPrefix, startNumber, startNumber + count - 1, totalRows, seatsPerRow);
        
        // Walidacja: sprawdź czy count nie jest za duży (zabezpieczenie przed przypadkowym utworzeniem tysięcy sal)
        if (count > 50) {
            log.warn("Próba utworzenia zbyt wielu sal na raz: {}", count);
            throw new IllegalArgumentException("Nie można utworzyć więcej niż 50 sal jednocześnie. Użyj mniejszej liczby.");
        }
        
        // Sprawdź z wyprzedzeniem czy wszystkie sale będą unikalne (optymalizacja)
        for (int i = 0; i < count; i++) {
            int roomNumber = startNumber + i;
            String roomNumberFull = roomNumberPrefix + " " + roomNumber;
            
            if (roomRepository.existsByRoomNumber(roomNumberFull)) {
                log.warn("Sala {} już istnieje, przerywam tworzenie", roomNumberFull);
                throw new RoomAlreadyExistsException("Sala o numerze '" + roomNumberFull + "' już istnieje. Operacja przerwana - żadna sala nie została utworzona.");
            }
        }
        
        // Wszystkie sale są unikalne - utwórz je wszystkie
        List<RoomDTO> createdRooms = new java.util.ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int roomNumber = startNumber + i;
            String roomNumberFull = roomNumberPrefix + " " + roomNumber;
            
            // Utwórz salę (createRoom również sprawdza unikalność, ale już wiemy że jest OK)
            CreateRoomDTO dto = CreateRoomDTO.builder()
                    .roomNumber(roomNumberFull)
                    .totalRows(totalRows)
                    .seatsPerRow(seatsPerRow)
                    .description(description)
                    .build();
            
            RoomDTO createdRoom = createRoom(dto);
            createdRooms.add(createdRoom);
        }
        
        log.info("Utworzono {} sal pomyślnie", createdRooms.size());
        return createdRooms;
    }
}

