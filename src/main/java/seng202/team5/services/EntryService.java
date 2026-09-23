package seng202.team5.services;

import seng202.team5.data.UserDAO;
import seng202.team5.exceptions.AuthenticationException;
import seng202.team5.models.EntryMode;
import seng202.team5.models.ErrorType;
import seng202.team5.models.User;
import seng202.team5.models.UserType;

import java.util.Objects;

/**
 * Validates entry input (username, password) and, once the input is
 * structurally valid, carries out the corresponding sign-in or
 * account-creation via {@link UserDAO}.
 *
 * The length/character bounds are public so the UI layer can read them
 * (e.g. for prompt text) instead of hardcoding a second copy that can
 * drift out of sync with the actual rules enforced here.
 */
public class EntryService {

    public static final int MIN_USERNAME_LENGTH = 5;
    public static final int MAX_USERNAME_LENGTH = 16;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 16;

    private final UserDAO userDAO;

    public EntryService() {
        this(new UserDAO());
    }

    /**
     * Package-private constructor allowing a mock/fake {@link UserDAO} to be
     * injected in tests, so validation logic can be tested without a live DB.
     */
    public EntryService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Validates the given entry input and, if it's structurally valid,
     * performs the sign-in or account-creation it describes.
     *
     * @param username        the username
     * @param password        the plaintext password
     * @param confirmPassword the confirm-password field's value (only
     *                        checked when {@code mode} is CREATE_ACCOUNT)
     * @param mode            whether this is a sign-in or account-creation attempt
     * @return a {@link ValidationResult}: on success, carries the resulting
     *         {@link User}; on failure, carries an {@link ErrorType} and a
     *         message describing what went wrong
     */
    public ValidationResult entryIsValid(String username, String password, String confirmPassword, EntryMode mode) {
        if (username == null || username.isEmpty()) {
            return ValidationResult.error(ErrorType.USERNAME,
                    "Please enter a username");
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            return ValidationResult.error(ErrorType.USERNAME,
                    "Username must be " + MIN_USERNAME_LENGTH + "+ characters long");
        }
        if (username.contains(" ") || username.contains("_") || username.contains(".")) {
            return ValidationResult.error(ErrorType.USERNAME,
                    "Username cannot contain spaces, \"_\", or \".\"");
        }
        if (password == null || password.isEmpty()) {
            return ValidationResult.error(ErrorType.PASSWORD,
                    "Please enter a password");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error(ErrorType.PASSWORD,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error(ErrorType.PASSWORD,
                    "Password must be no more than " + MAX_PASSWORD_LENGTH + " characters long");
        }
        if (!password.matches("[A-Za-z0-9!@#$%^&*]+")) {
            return ValidationResult.error(ErrorType.PASSWORD,
                    "Password must only contain letters, numbers and any of !@#$%^&*");
        }

        if (mode == EntryMode.SIGN_IN) {
            return signIn(username, password);
        }
        return createAccount(username, password, confirmPassword);
    }

    private ValidationResult signIn(String username, String password) {
        try {
            User currentUser = userDAO.authenticateUser(username, password);
            return ValidationResult.ok(currentUser);
        } catch (AuthenticationException e) {
            return ValidationResult.error(ErrorType.DATABASE, e.getMessage());
        }
    }

    private ValidationResult createAccount(String username, String password, String confirmPassword) {
        if (!Objects.equals(password, confirmPassword)) {
            return ValidationResult.error(ErrorType.PASSWORD, "Passwords do not match");
        }
        try {
            boolean registered = userDAO.registerUser(username, password, UserType.MEMBER);
            if (!registered) {
                return ValidationResult.error(ErrorType.DATABASE, "That username is already taken");
            }
            User currentUser = userDAO.authenticateUser(username, password);
            return ValidationResult.ok(currentUser);
        } catch (AuthenticationException e) {
            return ValidationResult.error(ErrorType.DATABASE, e.getMessage());
        }
    }
}