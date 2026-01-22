package pl.cinemaparadiso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Konfiguracja PasswordEncoder dla Spring Security
 * 
 * BCryptPasswordEncoder - algorytm hashujący hasła
 * - Jednokierunkowy (nie da się odzyskać hasła)
 * - Zawiera "salt" (każdy hash jest inny)
 * - Powolny (utrudnia ataki brute force)
 */
@Configuration
public class PasswordEncoderConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}



