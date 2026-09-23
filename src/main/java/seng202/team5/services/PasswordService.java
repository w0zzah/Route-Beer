package seng202.team5.services;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides password hashing and password verification.
 */
public class PasswordService {

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * @param plainTextPassword password to hash
     * @return BCrypt password hash
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(
                plainTextPassword,
                BCrypt.gensalt()
        );
    }

    /**
     * Checks a plaintext password against a stored BCrypt hash.
     *
     * @param plainTextPassword password supplied by the user
     * @param storedHash BCrypt hash stored in the database
     * @return true if the passwords match
     */
    public static boolean checkPassword(
            String plainTextPassword,
            String storedHash) {

        return BCrypt.checkpw(plainTextPassword, storedHash);
    }
}
