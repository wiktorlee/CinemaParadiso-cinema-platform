package pl.cinemaparadiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dla repertuaru - film z listą seansów w danym dniu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepertoireDTO {
    
    private Long movieId;
    private String movieTitle;
    private String moviePosterPath;
    private String movieGenre;
    private Integer movieDurationMinutes;
    private List<ScreeningTimeDTO> screenings; // Lista seansów z godzinami
}

