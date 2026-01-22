package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.ChangePasswordDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.service.UserService;

/**
 * Controller dla endpointów związanych z profilem użytkownika
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    
    /**
     * Zmienia hasło zalogowanego użytkownika
     * 
     * POST /api/profile/change-password
     * Wymaga zalogowania
     * 
     * @param dto - dane zmiany hasła (walidowane przez @Valid)
     * @return 200 OK jeśli sukces
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = getCurrentUserId();
        log.info("Zmiana hasła dla użytkownika ID: {}", userId);
        
        userService.changePassword(userId, dto);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Pobiera ID zalogowanego użytkownika z SecurityContext
     * 
     * @return ID użytkownika
     * @throws IllegalStateException jeśli użytkownik nie jest zalogowany
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Użytkownik nie jest zalogowany");
        }
        
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}


