package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy próbujemy zarejestrować użytkownika
 * z username, który już istnieje w bazie danych
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    
    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}



