package pl.cinemaparadiso.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.ReviewDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.repository.UserRepository;
import pl.cinemaparadiso.service.ReviewService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller dla endpointów związanych z recenzjami
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    
    /**
     * Usuwa recenzję (tylko własną)
     * 
     * DELETE /api/reviews/{id}
     * Wymaga zalogowania
     * 
     * @param id - ID recenzji
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("DELETE /api/reviews/{} - usuwanie recenzji", id);
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Pobiera recenzje zalogowanego użytkownika z paginacją
     * 
     * GET /api/reviews/my?page=0&size=10
     * Wymaga zalogowania
     * 
     * @param page - numer strony (domyślnie 0)
     * @param size - rozmiar strony (domyślnie 10)
     * @return strona z recenzjami użytkownika
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/reviews/my - pobieranie recenzji użytkownika");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie znaleziony"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviewsPage = reviewService.getUserReviews(user.getId(), pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", reviewsPage.getContent());
        response.put("totalElements", reviewsPage.getTotalElements());
        response.put("totalPages", reviewsPage.getTotalPages());
        response.put("currentPage", reviewsPage.getNumber());
        response.put("pageSize", reviewsPage.getSize());
        response.put("hasNext", reviewsPage.hasNext());
        response.put("hasPrevious", reviewsPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
}

