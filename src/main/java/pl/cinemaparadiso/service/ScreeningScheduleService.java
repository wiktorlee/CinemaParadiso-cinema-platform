package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.CreateScreeningScheduleDTO;
import pl.cinemaparadiso.dto.ScreeningDTO;
import pl.cinemaparadiso.dto.ScreeningScheduleDTO;
import pl.cinemaparadiso.dto.UpdateScreeningScheduleDTO;
import pl.cinemaparadiso.entity.Movie;
import pl.cinemaparadiso.entity.Room;
import pl.cinemaparadiso.entity.Screening;
import pl.cinemaparadiso.entity.ScreeningSchedule;
import pl.cinemaparadiso.exception.MovieNotFoundException;
import pl.cinemaparadiso.exception.RoomNotFoundException;
import pl.cinemaparadiso.exception.ScreeningConflictException;
import pl.cinemaparadiso.exception.ScreeningNotFoundException;
import pl.cinemaparadiso.repository.MovieRepository;
import pl.cinemaparadiso.repository.RoomRepository;
import pl.cinemaparadiso.repository.ScreeningRepository;
import pl.cinemaparadiso.repository.ScreeningScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania harmonogramami seansów
 * Zawiera logikę biznesową dla operacji CRUD na harmonogramach i automatyczne generowanie seansów
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScreeningScheduleService {
    
    private final ScreeningScheduleRepository scheduleRepository;
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ScreeningService screeningService;
    
    private static final int BREAK_TIME_MINUTES = 15; // Przerwa między seansami
    
    /**
     * Konwertuje encję ScreeningSchedule na DTO
     */
    private ScreeningScheduleDTO toDTO(ScreeningSchedule schedule) {
        // Pobierz liczbę wygenerowanych seansów z bazy
        int generatedCount = screeningRepository.findByMovieIdAndRoomId(
                schedule.getMovie().getId(), 
                schedule.getRoom().getId())
                .stream()
                .filter(s -> s.getGeneratedFromSchedule() != null && 
                           s.getGeneratedFromSchedule().getId().equals(schedule.getId()))
                .mapToInt(s -> 1)
                .sum();
        
        return ScreeningScheduleDTO.builder()
                .id(schedule.getId())
                .movieId(schedule.getMovie().getId())
                .movieTitle(schedule.getMovie().getTitle())
                .roomId(schedule.getRoom().getId())
                .roomNumber(schedule.getRoom().getRoomNumber())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .basePrice(schedule.getBasePrice())
                .vipPrice(schedule.getVipPrice())
                .generatedScreeningsCount(generatedCount)
                .build();
    }
    
    /**
     * Pobiera wszystkie harmonogramy
     */
    @Transactional(readOnly = true)
    public List<ScreeningScheduleDTO> getAllSchedules() {
        log.info("Pobieranie wszystkich harmonogramów");
        return scheduleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Pobiera harmonogram po ID
     */
    @Transactional(readOnly = true)
    public ScreeningScheduleDTO getScheduleById(Long id) {
        log.info("Pobieranie harmonogramu o ID: {}", id);
        ScreeningSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Harmonogram o ID " + id + " nie istnieje"));
        return toDTO(schedule);
    }
    
    /**
     * Tworzy nowy harmonogram
     */
    public ScreeningScheduleDTO createSchedule(CreateScreeningScheduleDTO dto) {
        log.info("Tworzenie nowego harmonogramu: film ID={}, sala ID={}, dzień={}, godzina={}", 
                dto.getMovieId(), dto.getRoomId(), dto.getDayOfWeek(), dto.getStartTime());
        
        // Walidacja dat
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Data zakończenia musi być późniejsza niż data rozpoczęcia");
        }
        
        // Sprawdź czy film istnieje
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(dto.getMovieId()));
        
        // Sprawdź czy sala istnieje
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(dto.getRoomId()));
        
        // Utwórz harmonogram
        ScreeningSchedule schedule = ScreeningSchedule.builder()
                .movie(movie)
                .room(room)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .basePrice(dto.getBasePrice())
                .vipPrice(dto.getVipPrice())
                .build();
        
        ScreeningSchedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Harmonogram utworzony pomyślnie: ID={}", savedSchedule.getId());
        
        return toDTO(savedSchedule);
    }
    
    /**
     * Aktualizuje harmonogram
     */
    public ScreeningScheduleDTO updateSchedule(Long id, UpdateScreeningScheduleDTO dto) {
        log.info("Aktualizacja harmonogramu o ID: {}", id);
        
        ScreeningSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Harmonogram o ID " + id + " nie istnieje"));
        
        // Aktualizuj pola
        if (dto.getDayOfWeek() != null) {
            schedule.setDayOfWeek(dto.getDayOfWeek());
        }
        if (dto.getStartTime() != null) {
            schedule.setStartTime(dto.getStartTime());
        }
        if (dto.getStartDate() != null) {
            schedule.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            schedule.setEndDate(dto.getEndDate());
        }
        if (dto.getBasePrice() != null) {
            schedule.setBasePrice(dto.getBasePrice());
        }
        if (dto.getVipPrice() != null) {
            schedule.setVipPrice(dto.getVipPrice());
        }
        
        // Walidacja dat
        if (schedule.getEndDate().isBefore(schedule.getStartDate())) {
            throw new IllegalArgumentException("Data zakończenia musi być późniejsza niż data rozpoczęcia");
        }
        
        ScreeningSchedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Harmonogram zaktualizowany pomyślnie: ID={}", updatedSchedule.getId());
        
        return toDTO(updatedSchedule);
    }
    
    /**
     * Usuwa harmonogram
     * UWAGA: Usunięcie harmonogramu nie usuwa wygenerowanych seansów!
     */
    public void deleteSchedule(Long id) {
        log.info("Usuwanie harmonogramu o ID: {}", id);
        
        ScreeningSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Harmonogram o ID " + id + " nie istnieje"));
        
        // Usuń referencję do harmonogramu z seansów (ale nie usuwaj seansów)
        List<Screening> generatedScreenings = screeningRepository.findByMovieId(schedule.getMovie().getId())
                .stream()
                .filter(s -> s.getGeneratedFromSchedule() != null && 
                           s.getGeneratedFromSchedule().getId().equals(schedule.getId()))
                .collect(Collectors.toList());
        
        for (Screening screening : generatedScreenings) {
            screening.setGeneratedFromSchedule(null);
            screeningRepository.save(screening);
        }
        
        scheduleRepository.delete(schedule);
        log.info("Harmonogram usunięty pomyślnie: ID={}", id);
    }
    
    /**
     * Generuje seanse z harmonogramu
     * Znajduje wszystkie daty pasujące do harmonogramu i tworzy seanse
     * 
     * @param scheduleId - ID harmonogramu
     * @return lista utworzonych seansów
     */
    public List<ScreeningDTO> generateScreeningsFromSchedule(Long scheduleId) {
        log.info("Generowanie seansów z harmonogramu ID: {}", scheduleId);
        
        ScreeningSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScreeningNotFoundException("Harmonogram o ID " + scheduleId + " nie istnieje"));
        
        // Znajdź wszystkie daty pasujące do harmonogramu
        List<LocalDate> matchingDates = findMatchingDates(schedule);
        
        log.info("Znaleziono {} dat pasujących do harmonogramu", matchingDates.size());
        
        List<ScreeningDTO> createdScreenings = new ArrayList<>();
        int skippedCount = 0;
        
        // Pobierz istniejące seanse z tego harmonogramu
        List<Screening> existingScreenings = screeningRepository.findByMovieId(schedule.getMovie().getId())
                .stream()
                .filter(s -> s.getGeneratedFromSchedule() != null && 
                           s.getGeneratedFromSchedule().getId().equals(schedule.getId()))
                .collect(Collectors.toList());
        
        // Dla każdej daty utwórz seans
        for (LocalDate date : matchingDates) {
            LocalDateTime screeningStartTime = LocalDateTime.of(date, schedule.getStartTime());
            
            // Sprawdź czy seans już istnieje (z tego harmonogramu)
            boolean alreadyExists = existingScreenings.stream()
                    .anyMatch(s -> s.getStartTime().equals(screeningStartTime));
            
            if (alreadyExists) {
                log.debug("Seans już istnieje: {}, pomijam", screeningStartTime);
                skippedCount++;
                continue;
            }
            
            // Sprawdź kolizje (używamy ScreeningService)
            try {
                // Utwórz DTO dla nowego seansu
                pl.cinemaparadiso.dto.CreateScreeningDTO createDto = 
                    pl.cinemaparadiso.dto.CreateScreeningDTO.builder()
                        .movieId(schedule.getMovie().getId())
                        .roomId(schedule.getRoom().getId())
                        .startTime(screeningStartTime)
                        .basePrice(schedule.getBasePrice())
                        .vipPrice(schedule.getVipPrice())
                        .build();
                
                // Utwórz seans (ScreeningService sprawdzi kolizje)
                ScreeningDTO createdScreening = screeningService.createScreening(createDto);
                
                // Ustaw referencję do harmonogramu
                Screening screening = screeningRepository.findById(createdScreening.getId())
                        .orElseThrow(() -> new RuntimeException("Nie można znaleźć utworzonego seansu"));
                screening.setGeneratedFromSchedule(schedule);
                screeningRepository.save(screening);
                
                createdScreenings.add(createdScreening);
                
            } catch (ScreeningConflictException e) {
                log.warn("Kolizja przy generowaniu seansu: {}, pomijam", screeningStartTime);
                skippedCount++;
            }
        }
        
        log.info("Wygenerowano {} seansów, pominięto {} (kolizje/duplikaty)", 
                createdScreenings.size(), skippedCount);
        
        return createdScreenings;
    }
    
    /**
     * Znajduje wszystkie daty pasujące do harmonogramu
     * Przykład: harmonogram na piątki, od 15.01 do 15.03 → zwróci wszystkie piątki w tym zakresie
     */
    private List<LocalDate> findMatchingDates(ScreeningSchedule schedule) {
        List<LocalDate> matchingDates = new ArrayList<>();
        
        // Konwertuj enum na DayOfWeek
        DayOfWeek targetDayOfWeek = convertToDayOfWeek(schedule.getDayOfWeek());
        
        LocalDate currentDate = schedule.getStartDate();
        LocalDate endDate = schedule.getEndDate();
        
        // Znajdź pierwszy dzień pasujący do harmonogramu
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (currentDate.getDayOfWeek() == targetDayOfWeek) {
                matchingDates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return matchingDates;
    }
    
    /**
     * Konwertuje ScheduleDayOfWeek na DayOfWeek
     */
    private DayOfWeek convertToDayOfWeek(pl.cinemaparadiso.enums.ScheduleDayOfWeek scheduleDay) {
        return switch (scheduleDay) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }
}

