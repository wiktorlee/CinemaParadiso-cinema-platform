package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy sala o podanym numerze już istnieje
 */
public class RoomAlreadyExistsException extends RuntimeException {
    
    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}

