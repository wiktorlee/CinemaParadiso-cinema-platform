package pl.cinemaparadiso.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT r FROM Reservation r WHERE r.screening.id = :screeningId AND r.status = 'PAID'")
    List<Reservation> findPaidReservationsByScreeningId(@Param("screeningId") Long screeningId);
    
    boolean existsByIdAndUserId(Long reservationId, Long userId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "LEFT JOIN FETCH r.reservationSeats " +
           "WHERE r.status = 'PAID'")
    List<Reservation> findAllPaidReservationsWithSeats();
    
    Optional<Reservation> findByQrCodeToken(String qrCodeToken);
    
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "LEFT JOIN FETCH r.reservationSeats " +
           "WHERE r.status = 'PAID' " +
           "AND ((r.paymentDate >= :startDate AND r.paymentDate < :endDate) " +
           "     OR (r.paymentDate IS NULL AND r.createdAt >= :startDate AND r.createdAt < :endDate))")
    List<Reservation> findPaidReservationsByPaymentDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}


