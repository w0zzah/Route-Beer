package seng202.team5.exceptions;

/**
 * Base class for failures that occur while authenticating or registering a user.
 * Unchecked, since most callers (controllers/UI) will want to catch this at a
 * single boundary rather than declare it on every method signature.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}