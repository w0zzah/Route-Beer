package seng202.team5.data;

import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.entry.PasswordService;

import java.sql.*;

/**
 * User Data Access Object.
 */
public class UserDAO {

    /**
     * Handles user authentication.
     *
     * @param username the username
     * @param password the plaintext password to check
     * @return the authenticated user - never null
     * @throws InvalidCredentialsException if the username does not exist, or exists
     *                                     but the password does not match. The same
     *                                     exception is used for both cases so callers
     *                                     cannot tell which one occurred.
     * @throws DataAccessException         if a database error occurs, or the stored
     *                                     user_type value cannot be parsed.
     */
    public User authenticateUser(String username, String password) {

        String query =
                """
                SELECT id, username, password, user_type 
                FROM users 
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new InvalidCredentialsException();
                }

                String storedHash = resultSet.getString("password");

                if (!PasswordService.checkPassword(password, storedHash)) {
                    throw new InvalidCredentialsException();
                }

                UserType type;
                try {
                    type = UserType.valueOf(resultSet.getString("user_type").toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new DataAccessException("Stored user_type could not be parsed", e);
                }

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        type);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to authenticate user", e);
        }
    }

    /**
     * Used to register a user.
     *
     * @param username the username
     * @param password the password
     * @param userType the user type
     * @return true if the user was registered
     * @throws DataAccessException if a database error occurs while registering
     */
    public boolean registerUser(String username, String password, UserType userType) {
        String query =
                        """
                INSERT INTO users (username, password, user_type)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, PasswordService.hashPassword(password));
            statement.setString(3, userType.name());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Username is already taken", e);
        }
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username
     * @return true if a row was deleted
     * @throws DataAccessException if a database error occurs while deleting
     */
    public boolean deleteUser(String username) {
        String query =
                """
                DELETE FROM users
                WHERE username = (?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user", e);
        }
    }
}