package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy seans nie zostanie znaleziony
 */
public class ScreeningNotFoundException extends RuntimeException {
    
    public ScreeningNotFoundException(String message) {
        super(message);
    }
    
    public ScreeningNotFoundException(Long id) {
        super("Seans o ID " + id + " nie istnieje");
    }
}

