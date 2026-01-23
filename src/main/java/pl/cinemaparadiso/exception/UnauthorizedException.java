package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy użytkownik nie ma uprawnień do wykonania operacji
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}

