package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy rezerwacja nie została znaleziona
 */
public class ReservationNotFoundException extends RuntimeException {
    
    public ReservationNotFoundException(String message) {
        super(message);
    }
}


