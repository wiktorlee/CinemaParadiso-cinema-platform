package pl.cinemaparadiso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO do masowego tworzenia sal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleRoomsDTO {
    
    @NotBlank(message = "Prefix numeru sali jest wymagany")
    private String roomNumberPrefix; // np. "Sala"
    
    @NotNull(message = "Numer początkowy jest wymagany")
    @Min(value = 1, message = "Numer początkowy musi być dodatni")
    private Integer startNumber; // np. 1
    
    @NotNull(message = "Liczba sal jest wymagana")
    @Positive(message = "Liczba sal musi być dodatnia")
    @Min(value = 1, message = "Liczba sal musi być co najmniej 1")
    @jakarta.validation.constraints.Max(value = 50, message = "Nie można utworzyć więcej niż 50 sal jednocześnie")
    private Integer count; // np. 5
    
    @NotNull(message = "Liczba rzędów jest wymagana")
    @Positive(message = "Liczba rzędów musi być dodatnia")
    private Integer totalRows;
    
    @NotNull(message = "Liczba miejsc w rzędzie jest wymagana")
    @Positive(message = "Liczba miejsc w rzędzie musi być dodatnia")
    private Integer seatsPerRow;
    
    private String description; // Opcjonalny opis
}

