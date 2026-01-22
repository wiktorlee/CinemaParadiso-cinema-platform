package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do tworzenia nowej sali
 * Zawiera walidację danych wejściowych
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomDTO {
    
    @NotBlank(message = "Numer sali jest wymagany")
    private String roomNumber;
    
    @NotNull(message = "Liczba rzędów jest wymagana")
    @Positive(message = "Liczba rzędów musi być dodatnia")
    private Integer totalRows;
    
    @NotNull(message = "Liczba miejsc w rzędzie jest wymagana")
    @Positive(message = "Liczba miejsc w rzędzie musi być dodatnia")
    private Integer seatsPerRow;
    
    private String description; // Opcjonalny opis sali
}

