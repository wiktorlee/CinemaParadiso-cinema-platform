package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dla sali kinowej
 * Zawiera podstawowe informacje o sali oraz listę miejsc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    
    private Long id;
    private String roomNumber;
    private Integer totalRows;
    private Integer seatsPerRow;
    private String description;
    private Integer capacity; // Obliczane: totalRows * seatsPerRow
    private List<SeatDTO> seats; // Lista miejsc w sali
}

