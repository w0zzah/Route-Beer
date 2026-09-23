package seng202.team5.unittests.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import seng202.team5.data.DatabaseConnection;
import seng202.team5.data.UserDAO;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.User;
import seng202.team5.models.UserType;
import seng202.team5.services.PasswordService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedDbConnection;
    private MockedStatic<PasswordService> mockedPasswordService;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDbConnection = mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        mockedPasswordService = mockStatic(PasswordService.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        userDAO = new UserDAO();
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
        mockedPasswordService.close();
    }

    @Test
    void authenticateUser_validCredentials_returnsUser() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn("hashedPassword");
        when(mockResultSet.getString("user_type")).thenReturn(UserType.ADMIN.name());
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("testUser");

        mockedPasswordService.when(() -> PasswordService.checkPassword("password123", "hashedPassword"))
                .thenReturn(true);

        User result = userDAO.authenticateUser("testUser", "password123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("testUser", result.getUsername());
        assertEquals(UserType.ADMIN, result.getType());
        verify(mockStatement).setString(1, "testUser");
    }

    @Test
    void authenticateUser_unknownUsername_throwsInvalidCredentialsException() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userDAO.authenticateUser("unknownUser", "password123"));
    }

    @Test
    void authenticateUser_wrongPassword_throwsInvalidCredentialsException() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn("hashedPassword");

        mockedPasswordService.when(() -> PasswordService.checkPassword("wrongPassword", "hashedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userDAO.authenticateUser("testUser", "wrongPassword"));
    }

    @Test
    void authenticateUser_invalidStoredUserType_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn("hashedPassword");
        when(mockResultSet.getString("user_type")).thenReturn("NOT_A_REAL_TYPE");

        mockedPasswordService.when(() -> PasswordService.checkPassword(anyString(), anyString()))
                .thenReturn(true);

        assertThrows(DataAccessException.class,
                () -> userDAO.authenticateUser("testUser", "password123"));
    }

    @Test
    void authenticateUser_sqlExceptionOnPrepare_throwsDataAccessException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB is down"));

        assertThrows(DataAccessException.class,
                () -> userDAO.authenticateUser("testUser", "password123"));
    }

    @Test
    void authenticateUser_sqlExceptionOnQuery_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class,
                () -> userDAO.authenticateUser("testUser", "password123"));
    }


    @Test
    void registerUser_success_returnsTrue() throws SQLException {
        UserType anyType = UserType.values()[0];
        when(mockStatement.executeUpdate()).thenReturn(1);
        mockedPasswordService.when(() -> PasswordService.hashPassword("password123"))
                .thenReturn("hashedPassword");

        boolean result = userDAO.registerUser("newUser", "password123", anyType);

        assertTrue(result);
        verify(mockStatement).setString(1, "newUser");
        verify(mockStatement).setString(2, "hashedPassword");
        verify(mockStatement).setString(3, anyType.name());
    }

    @Test
    void registerUser_noRowsAffected_returnsFalse() throws SQLException {
        UserType anyType = UserType.values()[0];
        when(mockStatement.executeUpdate()).thenReturn(0);
        mockedPasswordService.when(() -> PasswordService.hashPassword(anyString()))
                .thenReturn("hashedPassword");

        boolean result = userDAO.registerUser("newUser", "password123", anyType);

        assertFalse(result);
    }

    @Test
    void registerUser_sqlException_throwsDataAccessException() throws SQLException {
        UserType anyType = UserType.values()[0];
        mockedPasswordService.when(() -> PasswordService.hashPassword(anyString()))
                .thenReturn("hashedPassword");
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("constraint violation"));

        assertThrows(DataAccessException.class,
                () -> userDAO.registerUser("newUser", "password123", anyType));
    }

    @Test
    void deleteUser_success_returnsTrue() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.deleteUser("existingUser");

        assertTrue(result);
        verify(mockStatement).setString(1, "existingUser");
    }

    @Test
    void deleteUser_noMatchingUser_returnsFalse() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = userDAO.deleteUser("missingUser");

        assertFalse(result);
    }

    @Test
    void deleteUser_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(DataAccessException.class, () -> userDAO.deleteUser("someUser"));
    }
}