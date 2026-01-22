package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cinemaparadiso.enums.UserRole;

/**
 * DTO (Data Transfer Object) dla użytkownika - dane wyjściowe
 * 
 * Używany do zwracania informacji o użytkowniku w odpowiedziach HTTP
 * NIE zawiera hasła (bezpieczeństwo!)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;
    
    // NIE MA hasła - to jest celowe!
}



