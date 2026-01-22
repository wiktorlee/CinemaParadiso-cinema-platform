package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy film nie został znaleziony
 */
public class MovieNotFoundException extends RuntimeException {
    
    public MovieNotFoundException(Long id) {
        super("Film o ID " + id + " nie został znaleziony");
    }
    
    public MovieNotFoundException(String message) {
        super(message);
    }
}


