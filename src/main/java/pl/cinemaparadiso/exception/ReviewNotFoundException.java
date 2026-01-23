package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy recenzja nie została znaleziona
 */
public class ReviewNotFoundException extends RuntimeException {
    
    public ReviewNotFoundException(Long id) {
        super("Recenzja o ID " + id + " nie została znaleziona");
    }
}


