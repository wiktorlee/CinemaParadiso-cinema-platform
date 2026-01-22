package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.repository.UserRepository;

/**
 * Implementacja UserDetailsService dla Spring Security
 * 
 * Spring Security używa tej klasy do ładowania użytkownika podczas logowania
 * 
 * @Service - oznacza, że to jest komponent Spring
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Ładuje użytkownika po username
     * 
     * Spring Security wywołuje tę metodę podczas logowania
     * 
     * @param username - username użytkownika
     * @return UserDetails - użytkownik (nasza klasa User implementuje UserDetails)
     * @throws UsernameNotFoundException - jeśli użytkownik nie istnieje
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return user; // User implementuje UserDetails
    }
}



