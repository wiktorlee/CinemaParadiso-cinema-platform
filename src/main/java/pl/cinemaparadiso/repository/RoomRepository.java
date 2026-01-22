package pl.cinemaparadiso.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.cinemaparadiso.entity.Room;

import java.util.Optional;

/**
 * Repository dla encji Room (Sala)
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    /**
     * Sprawdza czy sala o podanym numerze już istnieje
     */
    boolean existsByRoomNumber(String roomNumber);
    
    /**
     * Znajduje salę po numerze
     */
    Optional<Room> findByRoomNumber(String roomNumber);
}

