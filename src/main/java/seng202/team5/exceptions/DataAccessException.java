package seng202.team5.exceptions;

/**
 * Thrown when a database failure (or unexpected data integrity problem, e.g.
 * an unrecognised stored user_type) prevents an operation from completing.
 * Always wraps the underlying cause so it isn't lost.
 */
public class DataAccessException extends AuthenticationException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}