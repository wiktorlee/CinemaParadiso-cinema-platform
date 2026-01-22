package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) dla rejestracji użytkownika
 * 
 * DTO służy do transferu danych między warstwami (Controller ↔ Service)
 * Zawiera tylko dane potrzebne do rejestracji
 * 
 * Walidacja:
 * - @NotBlank - pole nie może być puste
 * - @Size - minimalna/maksymalna długość
 * - @Pattern - format (regex)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    
    /**
     * Username użytkownika
     * Format: alphanumeric + underscore, min 3, max 20 znaków
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscore")
    private String username;
    
    /**
     * Hasło użytkownika
     * Bardzo podstawowa walidacja: min 6 znaków
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name is too long")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name is too long")
    private String lastName;
}



