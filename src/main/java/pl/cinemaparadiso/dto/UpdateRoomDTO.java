package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do aktualizacji sali
 * Tylko roomNumber i description można edytować (liczba rzędów/miejsc jest niezmienna)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomDTO {
    
    @NotBlank(message = "Numer sali jest wymagany")
    private String roomNumber;
    
    private String description; // Opcjonalny opis sali
}

