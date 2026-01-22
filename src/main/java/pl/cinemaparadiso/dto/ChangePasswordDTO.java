package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do zmiany hasła
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    
    @NotBlank(message = "Aktualne hasło jest wymagane")
    private String currentPassword;
    
    @NotBlank(message = "Nowe hasło jest wymagane")
    @Size(min = 6, message = "Nowe hasło musi mieć co najmniej 6 znaków")
    private String newPassword;
    
    @NotBlank(message = "Potwierdzenie hasła jest wymagane")
    private String confirmPassword;
}


