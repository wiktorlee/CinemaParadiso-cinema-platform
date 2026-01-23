package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO dla oceny filmu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRatingDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long movieId;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

