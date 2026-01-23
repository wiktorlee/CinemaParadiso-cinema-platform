package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.TicketAccessLog;

import java.util.List;

@Repository
public interface TicketAccessLogRepository extends JpaRepository<TicketAccessLog, Long> {
    
    List<TicketAccessLog> findByReservationIdOrderByCreatedAtDesc(Long reservationId);
    
    List<TicketAccessLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    long countByReservationId(Long reservationId);
}

