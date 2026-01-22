package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import pl.cinemaparadiso.enums.SeatType;

/**
 * Encja reprezentująca miejsce w sali kinowej
 */
@Entity
@Table(name = "seats")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne - wiele miejsc należy do jednej sali
     * @JoinColumn - kolumna w bazie danych, która przechowuje klucz obcy
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer rowNumber; // Numer rzędu (np. 1, 2, 3...)

    @Column(nullable = false)
    private Integer seatNumber; // Numer miejsca w rzędzie (np. 1, 2, 3...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType; // STANDARD lub VIP

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true; // Czy miejsce jest dostępne (false = zablokowane, np. uszkodzone)

    /**
     * @OneToMany - jedno miejsce może być w wielu rezerwacjach (ale w różnych seansach)
     */
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<ReservationSeat> reservationSeats = new java.util.ArrayList<>();

    /**
     * Unikalność: w jednej sali nie może być dwóch miejsc o tym samym rzędzie i numerze
     * To będzie sprawdzane w logice biznesowej lub przez unique constraint w bazie
     */
}

