package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.CreateReviewDTO;
import pl.cinemaparadiso.dto.ReviewDTO;
import pl.cinemaparadiso.entity.Movie;
import pl.cinemaparadiso.entity.Review;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.exception.MovieNotFoundException;
import pl.cinemaparadiso.exception.ReviewNotFoundException;
import pl.cinemaparadiso.exception.UnauthorizedException;
import pl.cinemaparadiso.repository.MovieRepository;
import pl.cinemaparadiso.repository.ReviewRepository;
import pl.cinemaparadiso.repository.UserRepository;

import java.util.Optional;

/**
 * Serwis do zarządzania recenzjami filmów
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    
    /**
     * Tworzy recenzję (lub aktualizuje istniejącą)
     */
    public ReviewDTO createReview(Long movieId, CreateReviewDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Musisz być zalogowany, aby napisać recenzję");
        }
        
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Użytkownik nie znaleziony"));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
        
        Optional<Review> existingReview = reviewRepository
                .findByUserIdAndMovieId(user.getId(), movieId);
        
        Review review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.setContent(dto.getContent());
            log.info("Aktualizacja recenzji filmu {} przez użytkownika {}", movieId, username);
        } else {
            review = Review.builder()
                    .user(user)
                    .movie(movie)
                    .content(dto.getContent())
                    .build();
            log.info("Nowa recenzja filmu {} przez użytkownika {}", movieId, username);
        }
        
        review = reviewRepository.save(review);
        return toDTO(review, user.getId());
    }
    
    /**
     * Pobiera recenzje filmu z paginacją
     */
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getMovieReviews(Long movieId, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                currentUserId = user.getId();
            }
        }
        
        final Long finalUserId = currentUserId;
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable)
                .map(review -> toDTO(review, finalUserId));
    }
    
    /**
     * Usuwa recenzję (tylko własną)
     */
    public void deleteReview(Long reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Musisz być zalogowany, aby usunąć recenzję");
        }
        
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Użytkownik nie znaleziony"));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        
        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Możesz usuwać tylko swoje recenzje");
        }
        
        reviewRepository.delete(review);
        log.info("Usunięcie recenzji {} przez użytkownika {}", reviewId, username);
    }
    
    /**
     * Pobiera recenzje użytkownika z paginacją
     */
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviewsPage.map(review -> toDTO(review, userId));
    }
    
    /**
     * Liczy wszystkie recenzje w systemie
     */
    @Transactional(readOnly = true)
    public long getTotalReviews() {
        return reviewRepository.count();
    }
    
    private ReviewDTO toDTO(Review review, Long currentUserId) {
        boolean isOwn = currentUserId != null && 
                       review.getUser().getId().equals(currentUserId);
        
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .firstName(review.getUser().getFirstName())
                .lastName(review.getUser().getLastName())
                .movieId(review.getMovie().getId())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isOwnReview(isOwn)
                .build();
    }
}

