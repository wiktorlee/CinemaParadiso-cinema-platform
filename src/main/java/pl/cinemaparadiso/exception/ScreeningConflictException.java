package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy seans koliduje z innym seansem (ta sama sala, ten sam czas)
 */
public class ScreeningConflictException extends RuntimeException {
    
    public ScreeningConflictException(String message) {
        super(message);
    }
    
    public ScreeningConflictException(Long roomId, String roomNumber, String conflictingTime) {
        super(String.format("Sala '%s' jest już zajęta w tym czasie. Konflikt: %s", roomNumber, conflictingTime));
    }
}

