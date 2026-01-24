package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.*;
import pl.cinemaparadiso.entity.*;
import pl.cinemaparadiso.enums.ReservationStatus;
import pl.cinemaparadiso.exception.*;
import pl.cinemaparadiso.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<SeatAvailabilityDTO> getAvailableSeats(Long screeningId) {
        log.debug("Pobieranie dostępnych miejsc dla seansu ID: {}", screeningId);
        
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ScreeningNotFoundException("Seans o ID " + screeningId + " nie istnieje"));
        
        Room room = screening.getRoom();
        List<Seat> allSeats = seatRepository.findByRoomIdOrderByRowNumberAscSeatNumberAsc(room.getId());
        
        List<ReservationSeat> reservedSeats = reservationSeatRepository.findReservedSeatsByScreeningId(screeningId);
        Set<Long> reservedSeatIds = reservedSeats.stream()
                .map(rs -> rs.getSeat().getId())
                .collect(Collectors.toSet());
        
        return allSeats.stream()
                .map(seat -> SeatAvailabilityDTO.builder()
                        .seatId(seat.getId())
                        .rowNumber(seat.getRowNumber())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType())
                        .isAvailable(!reservedSeatIds.contains(seat.getId()))
                        .isSeatEnabled(seat.getIsAvailable())
                        .build())
                .collect(Collectors.toList());
    }
    
    public ReservationDTO createReservation(CreateReservationDTO createDTO, Long userId) {
        log.info("Tworzenie rezerwacji dla użytkownika ID: {}, seans ID: {}", userId, createDTO.getScreeningId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + userId + " nie istnieje"));
        
        Screening screening = screeningRepository.findById(createDTO.getScreeningId())
                .orElseThrow(() -> new ScreeningNotFoundException("Seans o ID " + createDTO.getScreeningId() + " nie istnieje"));
        
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Nie można zarezerwować miejsc na seans, który już się rozpoczął");
        }
        
        List<ReservationSeat> reservationSeats = createDTO.getSeats().stream()
                .map(seatSelection -> {
                    Seat seat = seatRepository.findByIdWithLock(seatSelection.getSeatId())
                            .orElseThrow(() -> new IllegalArgumentException("Miejsce o ID " + seatSelection.getSeatId() + " nie istnieje"));
                    
                    if (!seat.getRoom().getId().equals(screening.getRoom().getId())) {
                        throw new IllegalArgumentException("Miejsce o ID " + seatSelection.getSeatId() + " nie należy do sali seansu");
                    }
                    
                    if (!seat.getIsAvailable()) {
                        throw new SeatNotAvailableException("Miejsce o ID " + seatSelection.getSeatId() + " jest niedostępne (zablokowane)");
                    }
                    
                    if (reservationSeatRepository.isSeatReservedForScreening(seat.getId(), screening.getId())) {
                        throw new SeatNotAvailableException("Miejsce rząd " + seat.getRowNumber() + ", miejsce " + seat.getSeatNumber() + " jest już zarezerwowane");
                    }
                    
                    boolean isVip = seat.getSeatType() == pl.cinemaparadiso.enums.SeatType.VIP;
                    BigDecimal price = screening.calculateFinalPrice(isVip, seatSelection.getTicketType());
                    
                    return ReservationSeat.builder()
                            .seat(seat)
                            .ticketType(seatSelection.getTicketType())
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());
        
        Reservation reservation = Reservation.builder()
                .user(user)
                .screening(screening)
                .createdAt(LocalDateTime.now())
                .status(ReservationStatus.PENDING_PAYMENT)
                .reservationSeats(reservationSeats)
                .build();
        
        reservationSeats.forEach(rs -> rs.setReservation(reservation));
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        log.info("Utworzono rezerwację ID: {} dla użytkownika ID: {}", savedReservation.getId(), userId);
        
        return toDTO(savedReservation);
    }
    
    @Transactional(readOnly = true)
    public List<ReservationDTO> getUserReservations(Long userId) {
        log.debug("Pobieranie rezerwacji użytkownika ID: {}", userId);
        
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reservations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(Long reservationId, Long userId) {
        log.debug("Pobieranie rezerwacji ID: {} dla użytkownika ID: {}", reservationId, userId);
        
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Rezerwacja o ID " + reservationId + " nie istnieje"));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do tej rezerwacji");
        }
        
        return toDTO(reservation);
    }
    
    public void verifySeatsAvailability(Reservation reservation) {
        log.debug("Weryfikacja dostępności miejsc dla rezerwacji ID: {}", reservation.getId());
        
        Long screeningId = reservation.getScreening().getId();
        
        for (ReservationSeat reservationSeat : reservation.getReservationSeats()) {
            Long seatId = reservationSeat.getSeat().getId();
            
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Miejsce o ID " + seatId + " nie istnieje (może zostało usunięte)"));
            
            if (!seat.getIsAvailable()) {
                throw new SeatNotAvailableException(
                        "Miejsce rząd " + seat.getRowNumber() + ", miejsce " + seat.getSeatNumber() + 
                        " jest niedostępne (zablokowane)");
            }
            
            boolean isReservedByOther = reservationSeatRepository.isSeatReservedByOtherReservation(
                    seatId, screeningId, reservation.getId());
            
            if (isReservedByOther) {
                throw new SeatNotAvailableException(
                        "Miejsce rząd " + seat.getRowNumber() + ", miejsce " + seat.getSeatNumber() + 
                        " zostało zajęte przez inną rezerwację");
            }
        }
        
        log.debug("Weryfikacja dostępności miejsc zakończona pomyślnie dla rezerwacji ID: {}", reservation.getId());
    }
    
    public void cancelReservation(Long reservationId, Long userId) {
        log.info("Anulowanie rezerwacji ID: {} przez użytkownika ID: {}", reservationId, userId);
        
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Rezerwacja o ID " + reservationId + " nie istnieje"));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do anulowania tej rezerwacji");
        }
        
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Rezerwacja jest już anulowana");
        }
        
        if (reservation.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Nie można anulować rezerwacji na seans, który już się rozpoczął");
        }
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        
        log.info("Anulowano rezerwację ID: {}", reservationId);
    }
    
    private ReservationDTO toDTO(Reservation reservation) {
        List<ReservationSeatDTO> seatDTOs = reservation.getReservationSeats().stream()
                .map(rs -> ReservationSeatDTO.builder()
                        .id(rs.getId())
                        .seatId(rs.getSeat().getId())
                        .rowNumber(rs.getSeat().getRowNumber())
                        .seatNumber(rs.getSeat().getSeatNumber())
                        .ticketType(rs.getTicketType())
                        .price(rs.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        return ReservationDTO.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .screeningId(reservation.getScreening().getId())
                .movieTitle(reservation.getScreening().getMovie().getTitle())
                .roomNumber(reservation.getScreening().getRoom().getRoomNumber())
                .screeningStartTime(reservation.getScreening().getStartTime())
                .createdAt(reservation.getCreatedAt())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .seats(seatDTOs)
                .paymentMethod(reservation.getPaymentMethod())
                .paymentDate(reservation.getPaymentDate())
                .paymentTransactionId(reservation.getPaymentTransactionId())
                .build();
    }
}

