package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.CreateScreeningDTO;
import pl.cinemaparadiso.dto.ScreeningDTO;
import pl.cinemaparadiso.dto.UpdateScreeningDTO;
import pl.cinemaparadiso.entity.Movie;
import pl.cinemaparadiso.entity.Room;
import pl.cinemaparadiso.entity.Screening;
import pl.cinemaparadiso.exception.MovieNotFoundException;
import pl.cinemaparadiso.exception.RoomNotFoundException;
import pl.cinemaparadiso.exception.ScreeningConflictException;
import pl.cinemaparadiso.exception.ScreeningNotFoundException;
import pl.cinemaparadiso.repository.MovieRepository;
import pl.cinemaparadiso.repository.ReservationSeatRepository;
import pl.cinemaparadiso.repository.RoomRepository;
import pl.cinemaparadiso.repository.ScreeningRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.cinemaparadiso.dto.RepertoireDTO;
import pl.cinemaparadiso.dto.ScreeningTimeDTO;

/**
 * Serwis do zarządzania seansami
 * Zawiera logikę biznesową dla operacji CRUD na seansach i walidację kolizji
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScreeningService {
    
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    
    private static final int BREAK_TIME_MINUTES = 15; // Przerwa między seansami
    
    /**
     * Konwertuje encję Screening na DTO
     */
    private ScreeningDTO toDTO(Screening screening) {
        LocalDateTime endTime = calculateEndTime(screening);
        int totalSeats = screening.getRoom().getTotalRows() * screening.getRoom().getSeatsPerRow();
        
        // Oblicz dostępne miejsca - policz zarezerwowane miejsca dla tego seansu
        List<pl.cinemaparadiso.entity.ReservationSeat> reservedSeats = 
                reservationSeatRepository.findReservedSeatsByScreeningId(screening.getId());
        int reservedCount = reservedSeats.size();
        int availableSeats = totalSeats - reservedCount;
        
        return ScreeningDTO.builder()
                .id(screening.getId())
                .movieId(screening.getMovie().getId())
                .movieTitle(screening.getMovie().getTitle())
                .movieDurationMinutes(screening.getMovie().getDurationMinutes())
                .roomId(screening.getRoom().getId())
                .roomNumber(screening.getRoom().getRoomNumber())
                .startTime(screening.getStartTime())
                .endTime(endTime)
                .basePrice(screening.getBasePrice())
                .vipPrice(screening.getVipPrice())
                .scheduleId(screening.getGeneratedFromSchedule() != null ? 
                           screening.getGeneratedFromSchedule().getId() : null)
                .availableSeats(availableSeats)
                .totalSeats(totalSeats)
                .build();
    }
    
    /**
     * Oblicza czas zakończenia seansu
     */
    private LocalDateTime calculateEndTime(Screening screening) {
        return screening.getStartTime()
                .plusMinutes(screening.getMovie().getDurationMinutes());
    }
    
    /**
     * Pobiera wszystkie seanse (z paginacją)
     */
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAllScreenings(Pageable pageable) {
        log.info("Pobieranie wszystkich seansów");
        return screeningRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    /**
     * Pobiera nadchodzące seanse (startTime >= teraz)
     */
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getUpcomingScreenings(Pageable pageable) {
        log.info("Pobieranie nadchodzących seansów");
        LocalDateTime now = LocalDateTime.now();
        return screeningRepository.findByStartTimeGreaterThanEqual(now, pageable)
                .map(this::toDTO);
    }
    
    /**
     * Pobiera seanse w zakresie dat
     */
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.info("Pobieranie seansów w zakresie: {} - {}", start, end);
        return screeningRepository.findByStartTimeBetween(start, end, pageable)
                .map(this::toDTO);
    }
    
    /**
     * Pobiera seanse dla danego filmu
     */
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByMovie(Long movieId, Pageable pageable) {
        log.info("Pobieranie seansów dla filmu ID: {}", movieId);
        LocalDateTime now = LocalDateTime.now();
        return screeningRepository.findByMovieIdAndStartTimeGreaterThanEqual(movieId, now, pageable)
                .map(this::toDTO);
    }
    
    /**
     * Pobiera seans po ID
     */
    @Transactional(readOnly = true)
    public ScreeningDTO getScreeningById(Long id) {
        log.info("Pobieranie seansu o ID: {}", id);
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException(id));
        return toDTO(screening);
    }
    
    /**
     * Tworzy nowy seans z walidacją kolizji
     */
    public ScreeningDTO createScreening(CreateScreeningDTO dto) {
        log.info("Tworzenie nowego seansu: film ID={}, sala ID={}, czas={}", 
                dto.getMovieId(), dto.getRoomId(), dto.getStartTime());
        
        // Sprawdź czy film istnieje
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(dto.getMovieId()));
        
        // Sprawdź czy sala istnieje
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(dto.getRoomId()));
        
        // Oblicz czas zakończenia seansu (z przerwą)
        LocalDateTime endTime = dto.getStartTime()
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(BREAK_TIME_MINUTES);
        
        // Sprawdź kolizje - czy sala jest wolna w tym czasie
        checkForConflicts(dto.getRoomId(), room.getRoomNumber(), dto.getStartTime(), endTime, null);
        
        // Utwórz nowy seans
        Screening screening = Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(dto.getStartTime())
                .basePrice(dto.getBasePrice())
                .vipPrice(dto.getVipPrice())
                .generatedFromSchedule(null) // Ręcznie utworzony
                .build();
        
        Screening savedScreening = screeningRepository.save(screening);
        log.info("Seans utworzony pomyślnie: ID={}, film={}, sala={}", 
                savedScreening.getId(), movie.getTitle(), room.getRoomNumber());
        
        return toDTO(savedScreening);
    }
    
    /**
     * Sprawdza czy sala jest wolna w danym czasie (walidacja kolizji)
     * 
     * @param roomId - ID sali
     * @param roomNumber - numer sali (dla komunikatu błędu)
     * @param startTime - czas rozpoczęcia nowego seansu
     * @param endTime - czas zakończenia nowego seansu (z przerwą)
     * @param excludeScreeningId - ID seansu do wykluczenia (przy aktualizacji)
     */
    private void checkForConflicts(Long roomId, String roomNumber, LocalDateTime startTime, 
                                   LocalDateTime endTime, Long excludeScreeningId) {
        // Pobierz wszystkie seanse w sali w zakresie dat (z marginesem)
        // Sprawdzamy zakres szerszy niż potrzebny, żeby złapać wszystkie możliwe kolizje
        LocalDateTime searchStart = startTime.minusDays(1);
        LocalDateTime searchEnd = endTime.plusDays(1);
        
        List<Screening> existingScreenings = screeningRepository.findByRoomIdAndStartTimeBetween(
                roomId, searchStart, searchEnd);
        
        // Sprawdź każdy seans czy koliduje
        for (Screening existing : existingScreenings) {
            // Pomiń seans, który aktualizujemy
            if (excludeScreeningId != null && existing.getId().equals(excludeScreeningId)) {
                continue;
            }
            
            LocalDateTime existingEndTime = existing.getStartTime()
                    .plusMinutes(existing.getMovie().getDurationMinutes())
                    .plusMinutes(BREAK_TIME_MINUTES);
            
            // Sprawdź kolizję: czy przedziały czasowe się nakładają
            boolean conflicts = (startTime.isBefore(existingEndTime) && endTime.isAfter(existing.getStartTime()));
            
            if (conflicts) {
                log.warn("Kolizja seansów: sala {} jest zajęta w czasie {} - {}", 
                        roomNumber, existing.getStartTime(), existingEndTime);
                throw new ScreeningConflictException(
                        roomId, roomNumber, 
                        String.format("%s - %s", existing.getStartTime(), existingEndTime));
            }
        }
    }
    
    /**
     * Aktualizuje seans
     * UWAGA: Nie można zmienić filmu/sali jeśli są już rezerwacje!
     */
    public ScreeningDTO updateScreening(Long id, UpdateScreeningDTO dto) {
        log.info("Aktualizacja seansu o ID: {}", id);
        
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException(id));
        
        // Sprawdź czy są rezerwacje (jeśli tak, ograniczone możliwości edycji)
        boolean hasReservations = !screening.getReservations().isEmpty();
        
        // Jeśli są rezerwacje, można zmienić tylko ceny
        if (hasReservations && dto.getStartTime() != null) {
            log.warn("Próba zmiany czasu seansu który ma rezerwacje: ID={}", id);
            throw new IllegalArgumentException("Nie można zmienić czasu seansu który ma rezerwacje");
        }
        
        // Aktualizuj czas rozpoczęcia (jeśli nie ma rezerwacji)
        if (dto.getStartTime() != null && !hasReservations) {
            // Sprawdź kolizje z nowym czasem
            LocalDateTime newEndTime = dto.getStartTime()
                    .plusMinutes(screening.getMovie().getDurationMinutes())
                    .plusMinutes(BREAK_TIME_MINUTES);
            
            checkForConflicts(screening.getRoom().getId(), 
                            screening.getRoom().getRoomNumber(),
                            dto.getStartTime(), newEndTime, id);
            
            screening.setStartTime(dto.getStartTime());
        }
        
        // Aktualizuj ceny
        if (dto.getBasePrice() != null) {
            screening.setBasePrice(dto.getBasePrice());
        }
        
        if (dto.getVipPrice() != null) {
            screening.setVipPrice(dto.getVipPrice());
        }
        
        Screening updatedScreening = screeningRepository.save(screening);
        log.info("Seans zaktualizowany pomyślnie: ID={}", updatedScreening.getId());
        
        return toDTO(updatedScreening);
    }
    
    /**
     * Usuwa seans
     * UWAGA: Nie można usunąć seansu który ma rezerwacje!
     */
    public void deleteScreening(Long id) {
        log.info("Usuwanie seansu o ID: {}", id);
        
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException(id));
        
        // Sprawdź czy są rezerwacje
        if (!screening.getReservations().isEmpty()) {
            log.warn("Próba usunięcia seansu który ma rezerwacje: ID={}", id);
            throw new IllegalArgumentException("Nie można usunąć seansu który ma rezerwacje");
        }
        
        screeningRepository.delete(screening);
        log.info("Seans usunięty pomyślnie: ID={}", id);
    }
    
    /**
     * Pobiera repertuar dla konkretnej daty
     * Zwraca listę filmów z ich seansami w danym dniu
     * Filtruje seanse które jeszcze się nie zakończyły (startTime + durationMinutes > teraz)
     */
    @Transactional(readOnly = true)
    public List<RepertoireDTO> getRepertoireByDate(LocalDate date) {
        log.info("Pobieranie repertuaru dla daty: {}", date);
        
        LocalDateTime now = LocalDateTime.now();
        
        // Konwertuj LocalDate na LocalDateTime (początek i koniec dnia)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        // Pobierz wszystkie seanse dla tego dnia (używamy Pageable.unpaged() aby pobrać wszystkie)
        List<Screening> screenings = screeningRepository.findByStartTimeBetween(startOfDay, endOfDay, Pageable.unpaged()).getContent();
        
        // Filtruj seanse które jeszcze się nie zakończyły
        // Seans się kończy: startTime + durationMinutes
        screenings = screenings.stream()
                .filter(s -> {
                    LocalDateTime screeningEndTime = s.getStartTime()
                            .plusMinutes(s.getMovie().getDurationMinutes());
                    return screeningEndTime.isAfter(now); // Seans jeszcze trwa lub się nie zaczął
                })
                .collect(Collectors.toList());
        
        // Pogrupuj seanse po filmach
        Map<Long, List<Screening>> screeningsByMovie = screenings.stream()
                .collect(Collectors.groupingBy(s -> s.getMovie().getId(), LinkedHashMap::new, Collectors.toList()));
        
        // Utwórz listę DTO dla każdego filmu
        List<RepertoireDTO> repertoire = new ArrayList<>();
        
        for (Map.Entry<Long, List<Screening>> entry : screeningsByMovie.entrySet()) {
            List<Screening> movieScreenings = entry.getValue();
            Screening firstScreening = movieScreenings.get(0);
            Movie movie = firstScreening.getMovie();
            
            // Utwórz listę godzin seansów dla tego filmu
            List<ScreeningTimeDTO> screeningTimes = movieScreenings.stream()
                    .map(s -> {
                        int totalSeats = s.getRoom().getTotalRows() * s.getRoom().getSeatsPerRow();
                        List<pl.cinemaparadiso.entity.ReservationSeat> reservedSeats = 
                                reservationSeatRepository.findReservedSeatsByScreeningId(s.getId());
                        int reservedCount = reservedSeats.size();
                        int availableSeats = totalSeats - reservedCount;
                        
                        return ScreeningTimeDTO.builder()
                                .screeningId(s.getId())
                                .startTime(s.getStartTime().toLocalTime())
                                .roomNumber(s.getRoom().getRoomNumber())
                                .basePrice(s.getBasePrice())
                                .vipPrice(s.getVipPrice())
                                .availableSeats(availableSeats)
                                .totalSeats(totalSeats)
                                .build();
                    })
                    .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                    .collect(Collectors.toList());
            
            // Utwórz DTO dla filmu
            RepertoireDTO repertoireDTO = RepertoireDTO.builder()
                    .movieId(movie.getId())
                    .movieTitle(movie.getTitle())
                    .moviePosterPath(movie.getPosterPath())
                    .movieGenre(movie.getGenre())
                    .movieDurationMinutes(movie.getDurationMinutes())
                    .screenings(screeningTimes)
                    .build();
            
            repertoire.add(repertoireDTO);
        }
        
        log.info("Znaleziono {} filmów w repertuarze dla daty {}", repertoire.size(), date);
        return repertoire;
    }
}

