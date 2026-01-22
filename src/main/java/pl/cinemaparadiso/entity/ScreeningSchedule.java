package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import pl.cinemaparadiso.enums.ScheduleDayOfWeek;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca harmonogram seansów
 * 
 * Używana do automatycznego generowania seansów.
 * 
 * Przykład:
 * - Film: "Incepcja"
 * - Sala: Sala 1
 * - Dzień tygodnia: PIĄTEK
 * - Godzina: 18:00
 * - Od: 15.01.2024
 * - Do: 15.03.2024
 * 
 * System automatycznie wygeneruje seanse:
 * - 19.01.2024 18:00
 * - 26.01.2024 18:00
 * - 02.02.2024 18:00
 * - ... (wszystkie piątki między datami)
 */
@Entity
@Table(name = "screening_schedules")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne - wiele harmonogramów może być dla jednego filmu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * @ManyToOne - wiele harmonogramów może być dla jednej sali
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     * Dzień tygodnia, w którym mają być generowane seanse
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleDayOfWeek dayOfWeek;

    /**
     * Godzina rozpoczęcia seansu
     */
    @Column(nullable = false)
    private LocalTime startTime;

    /**
     * Data rozpoczęcia generowania seansów
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Data zakończenia generowania seansów
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Cena bazowa dla seansów wygenerowanych z tego harmonogramu
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * Cena VIP dla seansów wygenerowanych z tego harmonogramu
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal vipPrice;

    /**
     * @OneToMany - jeden harmonogram może wygenerować wiele seansów
     * UWAGA: Nie używamy cascade = ALL ani orphanRemoval, bo nie chcemy usuwać seansów gdy usuwamy harmonogram
     */
    @OneToMany(mappedBy = "generatedFromSchedule")
    @Builder.Default
    private List<Screening> generatedScreenings = new ArrayList<>();
}

