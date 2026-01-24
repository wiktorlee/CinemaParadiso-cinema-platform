package pl.cinemaparadiso.controller;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.cinemaparadiso.entity.Reservation;
import pl.cinemaparadiso.entity.TicketAccessLog;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.exception.ReservationNotFoundException;
import pl.cinemaparadiso.repository.ReservationRepository;
import pl.cinemaparadiso.repository.TicketAccessLogRepository;
import pl.cinemaparadiso.repository.UserRepository;
import pl.cinemaparadiso.service.QRCodeService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final QRCodeService qrCodeService;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TicketAccessLogRepository ticketAccessLogRepository;

    @GetMapping(value = "/{reservationId}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getQRCode(@PathVariable Long reservationId) {
        log.info("Pobieranie QR code dla rezerwacji ID: {}", reservationId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));
        
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Rezerwacja o ID " + reservationId + " nie istnieje"));
        
        if (!reservation.getUser().getId().equals(user.getId())) {
            log.warn("Użytkownik {} próbował pobrać QR code dla cudzej rezerwacji {}", username, reservationId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (reservation.getStatus() != pl.cinemaparadiso.enums.ReservationStatus.PAID) {
            log.warn("Próba pobrania QR code dla nieopłaconej rezerwacji {}", reservationId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        if (reservation.getQrCodeToken() == null) {
            log.info("Generowanie nowego tokenu QR dla rezerwacji {}", reservationId);
            reservation.setQrCodeToken(UUID.randomUUID().toString());
            reservation.setQrCodeGeneratedAt(LocalDateTime.now());
            reservationRepository.save(reservation);
        }
        
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCodeWithURL(reservation.getQrCodeToken());
            
            logTicketAccess(reservation, user, "QR_DOWNLOAD");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            
            log.info("QR code wygenerowany pomyślnie dla rezerwacji {}", reservationId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCodeImage);
                    
        } catch (WriterException | IOException e) {
            log.error("Błąd podczas generowania QR code dla rezerwacji {}", reservationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyTicket(@RequestParam String token) {
        log.info("Weryfikacja biletu z tokenem: {}", token);
        
        Reservation reservation = reservationRepository.findByQrCodeToken(token)
                .orElse(null);
        
        if (reservation == null) {
            log.warn("Próba weryfikacji nieistniejącego biletu z tokenem: {}", token);
            return ResponseEntity.ok(java.util.Map.of(
                "valid", false,
                "message", "Bilet nie istnieje"
            ));
        }
        
        if (reservation.getStatus() != pl.cinemaparadiso.enums.ReservationStatus.PAID) {
            log.warn("Próba weryfikacji nieopłaconego biletu: {}", reservation.getId());
            return ResponseEntity.ok(java.util.Map.of(
                "valid", false,
                "message", "Bilet nie został opłacony"
            ));
        }
        
        log.info("Bilet zweryfikowany pomyślnie: {}", reservation.getId());
        
        logTicketAccess(reservation, null, "QR_VERIFY");
        
        return ResponseEntity.ok(java.util.Map.of(
            "valid", true,
            "reservationId", reservation.getId(),
            "message", "Bilet ważny"
        ));
    }
    
    @Transactional
    private void logTicketAccess(Reservation reservation, User user, String actionType) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();
            
            TicketAccessLog logEntry = TicketAccessLog.builder()
                    .reservation(reservation)
                    .user(user)
                    .actionType(actionType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            ticketAccessLogRepository.save(logEntry);
            log.debug("Zalogowano dostęp do QR code: reservationId={}, actionType={}, userId={}", 
                    reservation.getId(), actionType, user != null ? user.getId() : "null");
        } catch (Exception e) {
            log.warn("Błąd podczas logowania dostępu do QR code: {}", e.getMessage());
        }
    }
    
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                
                return ip;
            }
        } catch (Exception e) {
            log.warn("Błąd podczas pobierania IP adresu: {}", e.getMessage());
        }
        return null;
    }
    
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Błąd podczas pobierania User-Agent: {}", e.getMessage());
        }
        return null;
    }
}

