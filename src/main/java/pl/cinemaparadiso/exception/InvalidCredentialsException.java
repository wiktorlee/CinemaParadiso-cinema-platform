package pl.cinemaparadiso.exception;

/**
 * Wyjątek rzucany gdy podane dane logowania są nieprawidłowe
 * (nieprawidłowy username lub hasło)
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}



