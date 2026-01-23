package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.entity.Reservation;
import pl.cinemaparadiso.enums.PaymentMethod;
import pl.cinemaparadiso.enums.ReservationStatus;
import pl.cinemaparadiso.repository.ReservationRepository;
import pl.cinemaparadiso.repository.ReservationSeatRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serwis do obliczania statystyk dla panelu admina
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    
    /**
     * Pobiera wszystkie statystyki
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Podstawowe statystyki
        stats.put("totalRevenue", getTotalRevenue());
        stats.put("totalReservations", getTotalReservations());
        stats.put("paidReservations", getPaidReservationsCount());
        stats.put("totalReservedSeats", getTotalReservedSeats());
        
        // Statystyki z ostatnich 30 dni
        stats.put("revenueLast30Days", getRevenueForLastDays(30));
        stats.put("reservationsLast30Days", getReservationsCountForLastDays(30));
        
        // Statystyki dzienne z ostatnich 7 dni
        stats.put("dailyRevenueLast7Days", getDailyRevenueForLastDays(7));
        stats.put("dailyReservationsLast7Days", getDailyReservationsForLastDays(7));
        
        // Statystyki według metody płatności
        stats.put("revenueByPaymentMethod", getRevenueByPaymentMethod());
        stats.put("reservationsByPaymentMethod", getReservationsByPaymentMethod());
        
        // Statystyki z ostatnich 12 miesięcy (dla wykresu)
        stats.put("monthlyRevenue", getMonthlyRevenueForLastMonths(12));
        stats.put("monthlyReservations", getMonthlyReservationsForLastMonths(12));
        
        return stats;
    }
    
    /**
     * Oblicza całkowity zysk ze wszystkich opłaconych rezerwacji
     * Używa JOIN FETCH aby załadować reservationSeats (nie lazy loading)
     */
    public BigDecimal getTotalRevenue() {
        List<Reservation> paidReservations = reservationRepository.findAllPaidReservationsWithSeats();
        
        return paidReservations.stream()
                .map(Reservation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Liczba wszystkich rezerwacji
     */
    public long getTotalReservations() {
        return reservationRepository.count();
    }
    
    /**
     * Liczba opłaconych rezerwacji
     */
    public long getPaidReservationsCount() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PAID)
                .count();
    }
    
    /**
     * Liczba zarezerwowanych miejsc (z opłaconych rezerwacji)
     */
    public long getTotalReservedSeats() {
        return reservationSeatRepository.findAll().stream()
                .filter(rs -> rs.getReservation().getStatus() == ReservationStatus.PAID)
                .count();
    }
    
    /**
     * Zysk z ostatnich N dni
     */
    public BigDecimal getRevenueForLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1); // Do końca dzisiaj
        List<Reservation> paidReservations = reservationRepository
                .findPaidReservationsByPaymentDateRange(startDate, endDate);
        
        return paidReservations.stream()
                .map(Reservation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Liczba rezerwacji z ostatnich N dni
     */
    public long getReservationsCountForLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(startDate))
                .count();
    }
    
    /**
     * Dzienne zyski z ostatnich N dni
     * Używa paymentDate jeśli dostępne, w przeciwnym razie createdAt (dla starych rezerwacji)
     */
    public Map<String, BigDecimal> getDailyRevenueForLastDays(int days) {
        Map<String, BigDecimal> dailyRevenue = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Pobierz wszystkie opłacone rezerwacje z miejscami (raz, dla wydajności)
        List<Reservation> allPaidReservations = reservationRepository.findAllPaidReservationsWithSeats();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            // Filtruj rezerwacje dla tego dnia
            // Użyj paymentDate jeśli dostępne, w przeciwnym razie createdAt
            BigDecimal dayRevenue = allPaidReservations.stream()
                    .filter(r -> {
                        LocalDateTime dateToCheck = r.getPaymentDate() != null 
                                ? r.getPaymentDate() 
                                : r.getCreatedAt();
                        return dateToCheck.isAfter(startOfDay) && dateToCheck.isBefore(endOfDay);
                    })
                    .map(Reservation::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            dailyRevenue.put(date.toString(), dayRevenue);
        }
        
        return dailyRevenue;
    }
    
    /**
     * Dzienna liczba rezerwacji z ostatnich N dni
     */
    public Map<String, Long> getDailyReservationsForLastDays(int days) {
        Map<String, Long> dailyReservations = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            long count = reservationRepository.findAll().stream()
                    .filter(r -> r.getCreatedAt().isAfter(startOfDay) 
                            && r.getCreatedAt().isBefore(endOfDay))
                    .count();
            
            dailyReservations.put(date.toString(), count);
        }
        
        return dailyReservations;
    }
    
    /**
     * Zysk według metody płatności
     */
    public Map<String, BigDecimal> getRevenueByPaymentMethod() {
        Map<String, BigDecimal> revenueByMethod = new HashMap<>();
        
        List<Reservation> paidReservations = reservationRepository.findAllPaidReservationsWithSeats().stream()
                .filter(r -> r.getPaymentMethod() != null)
                .toList();
        
        for (PaymentMethod method : PaymentMethod.values()) {
            BigDecimal methodRevenue = paidReservations.stream()
                    .filter(r -> r.getPaymentMethod() == method)
                    .map(Reservation::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            revenueByMethod.put(method.name(), methodRevenue);
        }
        
        return revenueByMethod;
    }
    
    /**
     * Liczba rezerwacji według metody płatności
     */
    public Map<String, Long> getReservationsByPaymentMethod() {
        Map<String, Long> reservationsByMethod = new HashMap<>();
        
        List<Reservation> paidReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PAID 
                        && r.getPaymentMethod() != null)
                .toList();
        
        for (PaymentMethod method : PaymentMethod.values()) {
            long count = paidReservations.stream()
                    .filter(r -> r.getPaymentMethod() == method)
                    .count();
            
            reservationsByMethod.put(method.name(), count);
        }
        
        return reservationsByMethod;
    }
    
    /**
     * Miesięczne zyski z ostatnich N miesięcy
     * Używa paymentDate jeśli dostępne, w przeciwnym razie createdAt (dla starych rezerwacji)
     */
    public Map<String, BigDecimal> getMonthlyRevenueForLastMonths(int months) {
        Map<String, BigDecimal> monthlyRevenue = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Pobierz wszystkie opłacone rezerwacje z miejscami (raz, dla wydajności)
        List<Reservation> allPaidReservations = reservationRepository.findAllPaidReservationsWithSeats();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            LocalDateTime startOfMonth = monthStart.atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atStartOfDay();
            
            // Filtruj rezerwacje dla tego miesiąca
            // Użyj paymentDate jeśli dostępne, w przeciwnym razie createdAt
            BigDecimal monthRevenue = allPaidReservations.stream()
                    .filter(r -> {
                        LocalDateTime dateToCheck = r.getPaymentDate() != null 
                                ? r.getPaymentDate() 
                                : r.getCreatedAt();
                        return dateToCheck.isAfter(startOfMonth) && dateToCheck.isBefore(endOfMonth);
                    })
                    .map(Reservation::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String monthKey = monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue());
            monthlyRevenue.put(monthKey, monthRevenue);
        }
        
        return monthlyRevenue;
    }
    
    /**
     * Miesięczna liczba rezerwacji z ostatnich N miesięcy
     */
    public Map<String, Long> getMonthlyReservationsForLastMonths(int months) {
        Map<String, Long> monthlyReservations = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            LocalDateTime startOfMonth = monthStart.atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atStartOfDay();
            
            long count = reservationRepository.findAll().stream()
                    .filter(r -> r.getCreatedAt().isAfter(startOfMonth) 
                            && r.getCreatedAt().isBefore(endOfMonth))
                    .count();
            
            String monthKey = monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue());
            monthlyReservations.put(monthKey, count);
        }
        
        return monthlyReservations;
    }
}

