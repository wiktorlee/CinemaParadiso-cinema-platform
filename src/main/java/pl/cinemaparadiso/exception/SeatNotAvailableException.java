package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy miejsce nie jest dostępne do rezerwacji
 */
public class SeatNotAvailableException extends RuntimeException {
    
    public SeatNotAvailableException(String message) {
        super(message);
    }
}


