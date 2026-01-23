package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Encja reprezentująca ocenę filmu przez użytkownika
 * Jeden użytkownik może ocenić jeden film tylko raz (unikalność na poziomie bazy)
 */
@Entity
@Table(name = "movie_ratings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating; // 1-5 gwiazdek
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

