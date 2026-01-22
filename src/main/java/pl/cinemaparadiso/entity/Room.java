package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

/**
 * Encja reprezentująca salę kinową
 */
@Entity
@Table(name = "rooms")
@Audited  // Hibernate Envers - audit trail dla tej encji
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber; // Numer sali (np. "Sala 1", "Sala VIP")

    @Column(nullable = false)
    private Integer totalRows; // Liczba rzędów w sali

    @Column(nullable = false)
    private Integer seatsPerRow; // Liczba miejsc w każdym rzędzie

    @Column(length = 1000)
    private String description; // Opcjonalny opis sali (np. "Sala z systemem dźwięku Dolby Atmos")

    /**
     * @OneToMany - jedna sala ma wiele miejsc
     * cascade = CascadeType.ALL - operacje na sali wpływają na miejsca
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    /**
     * @OneToMany - jedna sala może mieć wiele seansów
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Screening> screenings = new ArrayList<>();

    /**
     * @OneToMany - jedna sala może mieć wiele harmonogramów
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScreeningSchedule> schedules = new ArrayList<>();
}

