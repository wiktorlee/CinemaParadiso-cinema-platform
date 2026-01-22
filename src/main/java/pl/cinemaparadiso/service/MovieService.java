package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.CreateMovieDTO;
import pl.cinemaparadiso.dto.MovieDTO;
import pl.cinemaparadiso.dto.UpdateMovieDTO;
import pl.cinemaparadiso.entity.Movie;
import pl.cinemaparadiso.exception.MovieAlreadyExistsException;
import pl.cinemaparadiso.exception.MovieNotFoundException;
import pl.cinemaparadiso.repository.MovieRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer dla filmów - logika biznesowa
 * 
 * @Service - oznacza, że to jest komponent Spring (warstwa logiki biznesowej)
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 * @Transactional - wszystkie metody są transakcyjne (automatyczny rollback przy błędzie)
 * @Slf4j - Lombok generuje logger (log.info(), log.error(), etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    /**
     * Konwertuje encję Movie na DTO
     * 
     * @param movie - encja Movie
     * @return MovieDTO
     */
    private MovieDTO toDTO(Movie movie) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .year(movie.getYear())
                .posterPath(movie.getPosterPath())
                .build();
    }
    
    /**
     * Pobiera wszystkie filmy (bez paginacji - dla kompatybilności wstecznej)
     * 
     * @return lista wszystkich filmów jako DTO
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        log.debug("Pobieranie wszystkich filmów");
        return movieRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Pobiera filmy z paginacją
     * 
     * @param pageable - parametry paginacji (strona, rozmiar, sortowanie)
     * @return strona z filmami jako DTO
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllMovies(Pageable pageable) {
        log.debug("Pobieranie filmów z paginacją: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return movieRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    /**
     * Wyszukuje filmy po tytule (z paginacją)
     * 
     * @param title - tytuł do wyszukania (częściowe dopasowanie, case-insensitive)
     * @param pageable - parametry paginacji
     * @return strona z filmami jako DTO
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> searchMoviesByTitle(String title, Pageable pageable) {
        log.debug("Wyszukiwanie filmów po tytule: '{}'", title);
        return movieRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::toDTO);
    }
    
    /**
     * Wyszukuje filmy po gatunku (z paginacją)
     * 
     * @param genre - gatunek filmu
     * @param pageable - parametry paginacji
     * @return strona z filmami jako DTO
     */
    @Transactional(readOnly = true)
    public Page<MovieDTO> searchMoviesByGenre(String genre, Pageable pageable) {
        log.debug("Wyszukiwanie filmów po gatunku: '{}'", genre);
        return movieRepository.findByGenre(genre, pageable)
                .map(this::toDTO);
    }
    
    /**
     * Pobiera film po ID
     * 
     * @param id - ID filmu
     * @return MovieDTO
     * @throws MovieNotFoundException jeśli film nie istnieje
     */
    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id) {
        log.debug("Pobieranie filmu o ID: {}", id);
        return movieRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }
    
    /**
     * Tworzy nowy film
     * 
     * @param dto - dane filmu do utworzenia
     * @return MovieDTO utworzonego filmu
     * @throws MovieAlreadyExistsException jeśli film o takim tytule już istnieje
     */
    public MovieDTO createMovie(CreateMovieDTO dto) {
        log.info("Tworzenie nowego filmu: {}", dto.getTitle());
        
        // Sprawdź czy film o takim tytule już istnieje
        if (movieRepository.existsByTitle(dto.getTitle())) {
            log.warn("Próba utworzenia filmu o istniejącym tytule: {}", dto.getTitle());
            throw new MovieAlreadyExistsException(dto.getTitle());
        }
        
        // Utwórz nową encję Movie
        Movie movie = Movie.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .genre(dto.getGenre())
                .director(dto.getDirector())
                .durationMinutes(dto.getDurationMinutes())
                .releaseDate(dto.getReleaseDate())
                .year(dto.getYear())
                .posterPath(null) // Okładka będzie dodana przez osobny endpoint
                .build();
        
        // Zapisz w bazie
        Movie savedMovie = movieRepository.save(movie);
        log.info("Film utworzony pomyślnie: ID={}, tytuł={}", savedMovie.getId(), savedMovie.getTitle());
        
        // Zwróć jako DTO
        return toDTO(savedMovie);
    }
    
    /**
     * Aktualizuje istniejący film
     * 
     * @param id - ID filmu do aktualizacji
     * @param dto - dane do aktualizacji (tylko nie-null pola są aktualizowane)
     * @return MovieDTO zaktualizowanego filmu
     * @throws MovieNotFoundException jeśli film nie istnieje
     * @throws MovieAlreadyExistsException jeśli nowy tytuł już istnieje
     */
    public MovieDTO updateMovie(Long id, UpdateMovieDTO dto) {
        log.info("Aktualizacja filmu o ID: {}", id);
        
        // Pobierz film z bazy
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        
        // Aktualizuj tylko te pola, które nie są null
        if (dto.getTitle() != null) {
            // Sprawdź czy nowy tytuł nie koliduje z innym filmem
            if (!movie.getTitle().equals(dto.getTitle()) && movieRepository.existsByTitle(dto.getTitle())) {
                log.warn("Próba zmiany tytułu na istniejący: {}", dto.getTitle());
                throw new MovieAlreadyExistsException(dto.getTitle());
            }
            movie.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            movie.setDescription(dto.getDescription());
        }
        if (dto.getGenre() != null) {
            movie.setGenre(dto.getGenre());
        }
        if (dto.getDirector() != null) {
            movie.setDirector(dto.getDirector());
        }
        if (dto.getDurationMinutes() != null) {
            movie.setDurationMinutes(dto.getDurationMinutes());
        }
        if (dto.getReleaseDate() != null) {
            movie.setReleaseDate(dto.getReleaseDate());
        }
        if (dto.getYear() != null) {
            movie.setYear(dto.getYear());
        }
        
        // Zapisz zmiany
        Movie updatedMovie = movieRepository.save(movie);
        log.info("Film zaktualizowany pomyślnie: ID={}, tytuł={}", updatedMovie.getId(), updatedMovie.getTitle());
        
        // Zwróć jako DTO
        return toDTO(updatedMovie);
    }
    
    /**
     * Usuwa film
     * 
     * @param id - ID filmu do usunięcia
     * @return ścieżka do pliku okładki (jeśli istnieje), null jeśli nie ma okładki
     * @throws MovieNotFoundException jeśli film nie istnieje
     */
    public String deleteMovie(Long id) {
        log.info("Usuwanie filmu o ID: {}", id);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        
        String posterPath = movie.getPosterPath();
        
        movieRepository.deleteById(id);
        log.info("Film usunięty pomyślnie: ID={}, tytuł={}", id, movie.getTitle());
        
        // Zwróć ścieżkę do pliku, aby można było go usunąć
        return posterPath;
    }
    
    /**
     * Aktualizuje ścieżkę do okładki filmu
     * 
     * @param id - ID filmu
     * @param posterPath - ścieżka do okładki (np. "/images/movies/inception.jpg")
     * @return MovieDTO zaktualizowanego filmu oraz stara ścieżka do pliku (jeśli istnieje)
     * @throws MovieNotFoundException jeśli film nie istnieje
     */
    public UpdatePosterResult updatePosterPath(Long id, String posterPath) {
        log.info("Aktualizacja okładki filmu o ID: {}", id);
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        
        String oldPosterPath = movie.getPosterPath();
        movie.setPosterPath(posterPath);
        Movie updatedMovie = movieRepository.save(movie);
        
        log.info("Okładka zaktualizowana: ID={}, nowa ścieżka={}", id, posterPath);
        
        return new UpdatePosterResult(toDTO(updatedMovie), oldPosterPath);
    }
    
    /**
     * Klasa pomocnicza do zwracania wyniku aktualizacji okładki
     */
    public static class UpdatePosterResult {
        private final MovieDTO movie;
        private final String oldPosterPath;
        
        public UpdatePosterResult(MovieDTO movie, String oldPosterPath) {
            this.movie = movie;
            this.oldPosterPath = oldPosterPath;
        }
        
        public MovieDTO getMovie() {
            return movie;
        }
        
        public String getOldPosterPath() {
            return oldPosterPath;
        }
    }
}

