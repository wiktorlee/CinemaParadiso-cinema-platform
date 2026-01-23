package pl.cinemaparadiso.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfiguracja Spring Security
 * 
 * @Configuration - oznacza, że to jest klasa konfiguracyjna Spring
 * @EnableWebSecurity - włącza Spring Security
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Włącza @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Konfiguruje SecurityFilterChain - główna konfiguracja zabezpieczeń
     * 
     * @param http - HttpSecurity do konfiguracji
     * @return SecurityFilterChain - łańcuch filtrów bezpieczeństwa
     * @throws Exception - jeśli konfiguracja się nie powiedzie
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Wyłącz CSRF dla REST API (lub skonfiguruj jeśli używasz cookies)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Konfiguracja autoryzacji (kto może co robić)
            .authorizeHttpRequests(auth -> auth
                // Statyczne pliki (HTML, CSS, JS) - dostęp dla wszystkich
                .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/profile.html", "/movies.html", "/repertuar.html", "/reservation.html", "/payment.html", "/admin/screenings.html", "/admin/audit-logs.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Endpointy autentykacji są publiczne (każdy może się zarejestrować/zalogować)
                .requestMatchers("/api/auth/**").permitAll()
                // GET /api/movies - lista filmów dostępna dla wszystkich
                .requestMatchers("/api/movies", "/api/movies/*").permitAll()
                // GET /api/screenings - lista seansów dostępna dla wszystkich (w tym repertuar)
                .requestMatchers("/api/screenings", "/api/screenings/*", "/api/screenings/upcoming", "/api/screenings/range", "/api/screenings/movie/*", "/api/screenings/repertoire").permitAll()
                // GET /api/reservations/screenings/*/seats - dostępne miejsca (publiczne)
                .requestMatchers("/api/reservations/screenings/*/seats").permitAll()
                // Panel admina - tylko dla ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Endpointy admina w API - tylko dla ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Wszystkie inne endpointy wymagają zalogowania
                .anyRequest().authenticated()
            )
            
            // Konfiguracja sesji
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED) // Twórz sesję jeśli potrzebna
                .maximumSessions(1) // Maksymalnie 1 sesja na użytkownika
                .maxSessionsPreventsLogin(false) // Nowa sesja wylogowuje starą
            );
        
        return http.build();
    }
    
    /**
     * Konfiguruje AuthenticationManager - zarządza procesem autentykacji
     * 
     * @return AuthenticationManager - menedżer autentykacji
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        
        return new ProviderManager(authProvider);
    }
}

