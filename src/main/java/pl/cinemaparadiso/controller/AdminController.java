package pl.cinemaparadiso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cinemaparadiso.dto.UserDTO;
import pl.cinemaparadiso.entity.User;
import pl.cinemaparadiso.enums.UserRole;
import pl.cinemaparadiso.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller dla operacji administracyjnych
 * Tylko ADMIN może używać tych endpointów
 * 
 * @RestController - oznacza, że to jest REST Controller (zwraca JSON)
 * @RequestMapping - prefiks dla wszystkich endpointów w tym kontrolerze
 * @RequiredArgsConstructor - Lombok generuje konstruktor z polami final
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Wszystkie endpointy w tym kontrolerze wymagają roli ADMIN
public class AdminController {
    
    private final UserRepository userRepository;
    
    /**
     * Pobiera wszystkich użytkowników
     * 
     * GET /api/admin/users
     * Tylko ADMIN
     * 
     * @return lista wszystkich użytkowników
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Zmienia rolę użytkownika
     * 
     * PUT /api/admin/users/{id}/role
     * Tylko ADMIN
     * 
     * @param id - ID użytkownika
     * @param role - nowa rola (USER lub ADMIN)
     * @return zaktualizowany użytkownik
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role) {
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik o ID " + id + " nie istnieje"));
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }
    
    /**
     * Mapuje Entity (User) na DTO (UserDTO)
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


