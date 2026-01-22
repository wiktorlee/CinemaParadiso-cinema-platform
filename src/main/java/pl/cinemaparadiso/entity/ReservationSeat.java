package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import pl.cinemaparadiso.enums.TicketType;

import java.math.BigDecimal;

/**
 * Encja reprezentująca miejsce w rezerwacji
 * 
 * Łączy rezerwację z konkretnym miejscem i przechowuje:
 * - które miejsce zostało zarezerwowane
 * - jaki typ biletu (NORMAL, REDUCED, STUDENT)
 * - finalną cenę (już obliczoną z uwzględnieniem zniżek)
 */
@Entity
@Table(name = "reservation_seats")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne - wiele miejsc w rezerwacji może należeć do jednej rezerwacji
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    /**
     * @ManyToOne - wiele miejsc w rezerwacji może być dla jednego miejsca (ale w różnych rezerwacjach/seansach)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    /**
     * Typ biletu (taryfa)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    /**
     * Finalna cena za to miejsce
     * Przechowujemy już obliczoną cenę (z uwzględnieniem zniżek, VIP, itp.)
     * Dzięki temu jeśli ceny się zmienią, stare rezerwacje zachowają swoje ceny
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}

