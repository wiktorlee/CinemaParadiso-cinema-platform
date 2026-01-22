package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pl.cinemaparadiso.dto.LoginDTO;
import pl.cinemaparadiso.dto.RegisterDTO;
import pl.cinemaparadiso.dto.UserDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.service.UserService;

/**
 * Controller odpowiedzialny za endpointy autentykacji
 * 
 * @RestController - oznacza, że to jest REST Controller (zwraca JSON)
 * @RequestMapping - prefiks dla wszystkich endpointów w tym kontrolerze
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Rejestracja nowego użytkownika
     * 
     * POST /api/auth/register
     * 
     * @param dto - dane rejestracji (walidowane przez @Valid)
     * @return ResponseEntity<UserDTO> - dane użytkownika (bez hasła)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO dto) {
        UserDTO user = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * Logowanie użytkownika
     * 
     * POST /api/auth/login
     * 
     * @param dto - dane logowania (walidowane przez @Valid)
     * @param request - HttpServletRequest do zapisania sesji
     * @return ResponseEntity<UserDTO> - dane zalogowanego użytkownika (bez hasła)
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        // 1. Utwórz token autentykacji
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        
        // 2. Spróbuj zalogować użytkownika (AuthenticationManager sprawdzi hasło)
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // 3. Zapisuj autentykację w SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 4. Zapisuj SecurityContext w sesji HTTP (ważne dla utrzymania sesji między requestami)
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext());
        
        // 5. Pobierz zalogowanego użytkownika
        User user = (User) authentication.getPrincipal();
        
        // 6. Zwróć dane użytkownika (bez hasła)
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
        
        return ResponseEntity.ok(userDTO);
    }
    
    /**
     * Pobiera informacje o zalogowanym użytkowniku
     * 
     * GET /api/auth/me
     * 
     * @return ResponseEntity<UserDTO> - dane zalogowanego użytkownika (bez hasła)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = (User) authentication.getPrincipal();
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
        
        return ResponseEntity.ok(userDTO);
    }
    
    /**
     * Wylogowanie użytkownika
     * 
     * POST /api/auth/logout
     * 
     * @param request - HttpServletRequest do unieważnienia sesji
     * @return ResponseEntity<Void> - potwierdzenie wylogowania
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}


