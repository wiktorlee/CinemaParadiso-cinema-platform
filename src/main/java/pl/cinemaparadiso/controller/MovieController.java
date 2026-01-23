package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.cinemaparadiso.dto.CreateMovieDTO;
import pl.cinemaparadiso.dto.MovieDTO;
import pl.cinemaparadiso.dto.UpdateMovieDTO;
import pl.cinemaparadiso.service.MovieService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller dla endpointów związanych z filmami
 * 
 * @RestController - oznacza, że to jest REST Controller (zwraca JSON)
 * @RequestMapping - prefiks dla wszystkich endpointów w tym kontrolerze
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 * @Slf4j - Lombok generuje logger
 */
@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    
    private final MovieService movieService;
    
    // Ścieżka do folderu z okładkami filmów
    private static final String POSTER_UPLOAD_DIR = "src/main/resources/static/images/movies/";
    
    /**
     * Pobiera wszystkie filmy (z paginacją opcjonalną)
     * 
     * GET /api/movies
     * GET /api/movies?page=0&size=20&sort=title,asc
     * GET /api/movies?search=tytul&genre=Science Fiction
     * 
     * @param page - numer strony (domyślnie 0)
     * @param size - rozmiar strony (domyślnie 20)
     * @param sort - sortowanie (domyślnie po tytule, asc)
     * @param search - wyszukiwanie po tytule (opcjonalne)
     * @param genre - filtrowanie po gatunku (opcjonalne)
     * @return lista filmów lub strona z filmami (jeśli użyto paginacji)
     */
    @GetMapping
    public ResponseEntity<?> getAllMovies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "title,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre) {
        
        // Jeśli nie podano parametrów paginacji, zwróć wszystkie filmy (dla kompatybilności wstecznej)
        if (page == null && size == null && search == null && genre == null) {
            List<MovieDTO> movies = movieService.getAllMovies();
            return ResponseEntity.ok(movies);
        }
        
        // Utwórz Pageable z domyślnymi wartościami
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 12; // Zmniejszone z 20 na 12 dla szybszego ładowania
        
        // Parsuj sortowanie (format: "field,direction" np. "title,asc")
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        
        Page<MovieDTO> moviesPage;
        
        // Wyszukiwanie po tytule
        if (search != null && !search.trim().isEmpty()) {
            moviesPage = movieService.searchMoviesByTitle(search.trim(), pageable);
        }
        // Filtrowanie po gatunku
        else if (genre != null && !genre.trim().isEmpty()) {
            moviesPage = movieService.searchMoviesByGenre(genre.trim(), pageable);
        }
        // Wszystkie filmy z paginacją
        else {
            moviesPage = movieService.getAllMovies(pageable);
        }
        
        // Zwróć odpowiedź z metadanymi paginacji
        Map<String, Object> response = new HashMap<>();
        response.put("content", moviesPage.getContent());
        response.put("totalElements", moviesPage.getTotalElements());
        response.put("totalPages", moviesPage.getTotalPages());
        response.put("currentPage", moviesPage.getNumber());
        response.put("pageSize", moviesPage.getSize());
        response.put("hasNext", moviesPage.hasNext());
        response.put("hasPrevious", moviesPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Pobiera film po ID
     * 
     * GET /api/movies/{id}
     * 
     * @param id - ID filmu
     * @return film (404 jeśli nie znaleziono - obsługiwane przez GlobalExceptionHandler)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * Tworzy nowy film
     * 
     * POST /api/movies
     * Tylko ADMIN może tworzyć filmy
     * 
     * @param dto - dane filmu do utworzenia
     * @return utworzony film
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> createMovie(@Valid @RequestBody CreateMovieDTO dto) {
        MovieDTO movie = movieService.createMovie(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }
    
    /**
     * Aktualizuje istniejący film
     * 
     * PUT /api/movies/{id}
     * Tylko ADMIN może aktualizować filmy
     * 
     * @param id - ID filmu do aktualizacji
     * @param dto - dane do aktualizacji
     * @return zaktualizowany film (404/409 jeśli błąd - obsługiwane przez GlobalExceptionHandler)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @Valid @RequestBody UpdateMovieDTO dto) {
        MovieDTO movie = movieService.updateMovie(id, dto);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * Usuwa film i jego okładkę z dysku
     * 
     * DELETE /api/movies/{id}
     * Tylko ADMIN może usuwać filmy
     * 
     * @param id - ID filmu do usunięcia
     * @return 204 No Content jeśli sukces (404 jeśli nie znaleziono - obsługiwane przez GlobalExceptionHandler)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        // Usuń film z bazy (zwraca ścieżkę do pliku okładki)
        String posterPath = movieService.deleteMovie(id);
        
        // Usuń plik okładki z dysku jeśli istnieje
        if (posterPath != null && !posterPath.isEmpty()) {
            deletePosterFile(posterPath);
        }
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Usuwa plik okładki z dysku
     * 
     * @param posterPath - ścieżka do pliku (np. "/images/movies/filename.jpg")
     */
    private void deletePosterFile(String posterPath) {
        try {
            // Wyciągnij nazwę pliku ze ścieżki (np. "/images/movies/filename.jpg" -> "filename.jpg")
            String filename = posterPath.substring(posterPath.lastIndexOf("/") + 1);
            Path filePath = Paths.get(POSTER_UPLOAD_DIR).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Usunięto plik okładki: {}", filePath);
            } else {
                log.warn("Plik okładki nie istnieje: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Błąd podczas usuwania pliku okładki: {}", posterPath, e);
            // Nie rzucamy wyjątku - usunięcie filmu z bazy jest ważniejsze
        }
    }
    
    /**
     * Upload okładki filmu
     * 
     * POST /api/movies/{id}/poster
     * Tylko ADMIN może uploadować okładki
     * 
     * @param id - ID filmu
     * @param file - plik obrazu (PNG, JPG, etc.)
     * @return zaktualizowany film z nową ścieżką do okładki
     */
    @PostMapping(value = "/{id}/poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> uploadPoster(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        // Walidacja pliku
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Sprawdź typ pliku (tylko obrazy)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Pobierz film aby sprawdzić czy istnieje i uzyskać starą ścieżkę okładki
            MovieDTO currentMovie = movieService.getMovieById(id);
            String oldPosterPath = currentMovie.getPosterPath();
            
            // Utwórz folder jeśli nie istnieje
            Path uploadPath = Paths.get(POSTER_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generuj unikalną nazwę pliku (UUID + oryginalna nazwa)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // Zapisz nowy plik na dysku
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Ścieżka względna do użycia w URL (Spring Boot serwuje pliki z static/)
            String posterPath = "/images/movies/" + filename;
            
            // Zaktualizuj film w bazie z nową ścieżką
            MovieService.UpdatePosterResult result = movieService.updatePosterPath(id, posterPath);
            
            // Usuń stary plik okładki jeśli istniał
            if (oldPosterPath != null && !oldPosterPath.isEmpty()) {
                deletePosterFile(oldPosterPath);
            }
            
            return ResponseEntity.ok(result.getMovie());
            
        } catch (IOException e) {
            log.error("Błąd podczas uploadu okładki dla filmu ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

