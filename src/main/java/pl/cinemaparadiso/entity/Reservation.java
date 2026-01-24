package pl.cinemaparadiso.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import pl.cinemaparadiso.enums.PaymentMethod;
import pl.cinemaparadiso.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @Column(name = "qr_code_token", unique = true)
    private String qrCodeToken;

    @Column(name = "qr_code_generated_at")
    private LocalDateTime qrCodeGeneratedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReservationSeat> reservationSeats = new ArrayList<>();

    public BigDecimal getTotalPrice() {
        return reservationSeats.stream()
                .map(ReservationSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

