package seng202.team5.services;

import seng202.team5.models.ErrorType;
import seng202.team5.models.User;

/**
 * Result of validating a piece of entry input (username, password, etc).
 * Replaces returning a plain String and comparing it against a magic
 * "Valid" literal, which is easy to typo and gives no compile-time safety.
 */
public record ValidationResult(boolean valid, User user, ErrorType type, String message) {

    public static ValidationResult ok(User user) {
        return new ValidationResult(true, user, null, null);
    }

    public static ValidationResult error(ErrorType type, String message) {
        return new ValidationResult(false, null, type, message);
    }
}