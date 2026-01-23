package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.MovieRatingDTO;
import pl.cinemaparadiso.dto.RateMovieDTO;
import pl.cinemaparadiso.entity.Movie;
import pl.cinemaparadiso.entity.MovieRating;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.exception.MovieNotFoundException;
import pl.cinemaparadiso.exception.UnauthorizedException;
import pl.cinemaparadiso.repository.MovieRatingRepository;
import pl.cinemaparadiso.repository.MovieRepository;
import pl.cinemaparadiso.repository.UserRepository;

import java.util.Optional;

/**
 * Serwis do zarządzania ocenami filmów
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MovieRatingService {
    
    private final MovieRatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    
    /**
     * Ocenia film (lub aktualizuje istniejącą ocenę)
     */
    public MovieRatingDTO rateMovie(Long movieId, RateMovieDTO dto) {
        // Pobierz zalogowanego użytkownika
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Musisz być zalogowany, aby ocenić film");
        }
        
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Użytkownik nie znaleziony"));
        
        // Sprawdź czy film istnieje
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        
        // Sprawdź czy użytkownik już ocenił ten film
        Optional<MovieRating> existingRating = ratingRepository
                .findByUserIdAndMovieId(user.getId(), movieId);
        
        MovieRating rating;
        if (existingRating.isPresent()) {
            // Aktualizuj istniejącą ocenę
            rating = existingRating.get();
            rating.setRating(dto.getRating());
            log.info("Aktualizacja oceny filmu {} przez użytkownika {}: {} gwiazdek", 
                    movieId, username, dto.getRating());
        } else {
            // Utwórz nową ocenę
            rating = MovieRating.builder()
                    .user(user)
                    .movie(movie)
                    .rating(dto.getRating())
                    .build();
            log.info("Nowa ocena filmu {} przez użytkownika {}: {} gwiazdek", 
                    movieId, username, dto.getRating());
        }
        
        rating = ratingRepository.save(rating);
        return toDTO(rating);
    }
    
    /**
     * Pobiera ocenę użytkownika dla filmu
     */
    @Transactional(readOnly = true)
    public Optional<Integer> getUserRating(Long movieId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        
        return ratingRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .map(MovieRating::getRating);
    }
    
    /**
     * Pobiera średnią ocenę filmu
     */
    @Transactional(readOnly = true)
    public Double getAverageRating(Long movieId) {
        return ratingRepository.getAverageRatingByMovieId(movieId);
    }
    
    /**
     * Pobiera liczbę ocen filmu
     */
    @Transactional(readOnly = true)
    public Long getRatingCount(Long movieId) {
        return ratingRepository.getRatingCountByMovieId(movieId);
    }
    
    /**
     * Liczy wszystkie oceny w systemie
     */
    @Transactional(readOnly = true)
    public long getTotalRatings() {
        return ratingRepository.count();
    }
    
    private MovieRatingDTO toDTO(MovieRating rating) {
        return MovieRatingDTO.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .movieId(rating.getMovie().getId())
                .rating(rating.getRating())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}


