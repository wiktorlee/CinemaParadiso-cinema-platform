package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca seans (konkretny pokaz filmu w konkretnej sali o konkretnej godzinie)
 * 
 * Seans może być utworzony ręcznie lub automatycznie wygenerowany z harmonogramu (ScreeningSchedule)
 */
@Entity
@Table(name = "screenings")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne - wiele seansów może być dla jednego filmu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * @ManyToOne - wiele seansów może być w jednej sali
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * Data i godzina rozpoczęcia seansu
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * Cena bazowa biletu (standardowe miejsce, normalny bilet)
     * BigDecimal - używamy do przechowywania pieniędzy (precyzja, unikanie błędów zaokrągleń)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * Cena dla miejsc VIP (dodatkowa opłata za VIP)
     * Jeśli null, miejsca VIP kosztują tyle samo co standardowe
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal vipPrice;

    /**
     * Opcjonalne: referencja do harmonogramu, z którego został wygenerowany ten seans
     * null jeśli został utworzony ręcznie
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ScreeningSchedule generatedFromSchedule;

    /**
     * @OneToMany - jeden seans może mieć wiele rezerwacji
     */
    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    /**
     * Metoda pomocnicza do obliczania ceny końcowej
     * (będzie używana w logice biznesowej)
     */
    public BigDecimal calculateFinalPrice(boolean isVip, pl.cinemaparadiso.enums.TicketType ticketType) {
        BigDecimal price = isVip && vipPrice != null ? vipPrice : basePrice;
        
        // Zniżki dla różnych typów biletów
        switch (ticketType) {
            case REDUCED -> price = price.multiply(new BigDecimal("0.8")); // 20% zniżki
            case STUDENT -> price = price.multiply(new BigDecimal("0.7")); // 30% zniżki
            case NORMAL -> {} // Brak zniżki
        }
        
        return price;
    }
}

