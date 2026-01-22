package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do duplikowania sali
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateRoomDTO {
    
    @NotBlank(message = "Numer nowej sali jest wymagany")
    private String newRoomNumber;
}

