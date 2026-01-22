package pl.cinemaparadiso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cinemaparadiso.dto.RegisterDTO;
import pl.cinemaparadiso.dto.UserDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.enums.UserRole;
import pl.cinemaparadiso.exception.UsernameAlreadyExistsException;
import pl.cinemaparadiso.repository.UserRepository;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z użytkownikami
 * 
 * @Service - oznacza, że to jest komponent Spring (warstwa biznesowa)
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 * @Transactional - wszystkie metody są transakcyjne (atomowe operacje na bazie)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Rejestruje nowego użytkownika
     * 
     * @param dto - dane rejestracji
     * @return UserDTO - dane użytkownika (bez hasła)
     * @throws UsernameAlreadyExistsException - jeśli username już istnieje
     */
    public UserDTO register(RegisterDTO dto) {
        // 1. Sprawdź czy username już istnieje
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException(dto.getUsername());
        }
        
        // 2. Hashuj hasło (NIGDY nie przechowujemy plain text!)
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        
        // 3. Utwórz użytkownika
        User user = User.builder()
                .username(dto.getUsername())
                .password(hashedPassword)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(UserRole.USER) // Domyślnie USER
                .build();
        
        // 4. Zapisz do bazy danych
        User savedUser = userRepository.save(user);
        
        // 5. Zwróć DTO (bez hasła!)
        return mapToDTO(savedUser);
    }
    
    /**
     * Znajduje użytkownika po username
     * 
     * @param username - username użytkownika
     * @return UserDTO - dane użytkownika (bez hasła)
     */
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return mapToDTO(user);
    }
    
    /**
     * Mapuje Entity (User) na DTO (UserDTO)
     * 
     * @param user - encja użytkownika
     * @return UserDTO - DTO użytkownika (bez hasła)
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}



