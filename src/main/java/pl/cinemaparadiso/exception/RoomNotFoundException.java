package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy sala nie zostanie znaleziona
 */
public class RoomNotFoundException extends RuntimeException {
    
    public RoomNotFoundException(String message) {
        super(message);
    }
    
    public RoomNotFoundException(Long id) {
        super("Sala o ID " + id + " nie istnieje");
    }
}

