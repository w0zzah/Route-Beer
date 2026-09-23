package seng202.team5.exceptions;
/**
 * Thrown when a login attempt fails because the username does not exist,
 * or exists but the supplied password is wrong.
 *
 * Deliberately used for BOTH cases (rather than having separate
 * UsernameNotFoundException / WrongPasswordException) so that the caller,
 * and ultimately the UI, cannot distinguish "unknown username" from
 * "wrong password" - this avoids leaking which usernames are registered.
 */
public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}