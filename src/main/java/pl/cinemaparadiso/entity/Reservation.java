package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import pl.cinemaparadiso.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca rezerwację
 * 
 * Jedna rezerwacja może zawierać wiele miejsc (ReservationSeat)
 * Przykład: Użytkownik rezerwuje 3 miejsca na jeden seans
 */
@Entity
@Table(name = "reservations")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne - wiele rezerwacji może należeć do jednego użytkownika
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * @ManyToOne - wiele rezerwacji może być dla jednego seansu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    /**
     * Data utworzenia rezerwacji
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Status rezerwacji
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.ACTIVE;

    /**
     * @OneToMany - jedna rezerwacja może zawierać wiele miejsc
     * cascade = CascadeType.ALL - operacje na rezerwacji wpływają na miejsca
     */
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReservationSeat> reservationSeats = new ArrayList<>();

    /**
     * Metoda pomocnicza do obliczania całkowitej ceny rezerwacji
     */
    public BigDecimal getTotalPrice() {
        return reservationSeats.stream()
                .map(ReservationSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

