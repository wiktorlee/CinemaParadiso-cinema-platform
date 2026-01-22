package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy film o danym tytule już istnieje
 */
public class MovieAlreadyExistsException extends RuntimeException {
    
    public MovieAlreadyExistsException(String title) {
        super("Film o tytule '" + title + "' już istnieje");
    }
    
    public MovieAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}


