package seng202.team5.unittests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import seng202.team5.data.UserDAO;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.types.EntryMode;
import seng202.team5.models.types.ErrorType;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.entry.EntryService;
import seng202.team5.services.entry.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    private static final String VALID_USERNAME = "validUser";
    private static final String VALID_PASSWORD = "ValidPass1!";

    @Mock
    private UserDAO userDAO;

    private EntryService entryService;

    @BeforeEach
    void setUp() {
        entryService = new EntryService(userDAO);
    }

    @Nested
    class UsernameValidation {

        @Test
        void nullUsername_isRejected() {
            ValidationResult result =
                    entryService.entryIsValid(null, VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.USERNAME, result.type());
            verifyNoInteractions(userDAO);
        }

        @Test
        void emptyUsername_isRejected() {
            ValidationResult result =
                    entryService.entryIsValid("", VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.USERNAME, result.type());
            assertEquals("Please enter a username", result.message());
        }

        @Test
        void usernameBelowMinLength_isRejected() {
            String tooShort = "a".repeat(EntryService.MIN_USERNAME_LENGTH - 1);

            ValidationResult result =
                    entryService.entryIsValid(tooShort, VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.USERNAME, result.type());
        }

        @Test
        void usernameAtMinLength_isAccepted() {
            String exactlyMin = "a".repeat(EntryService.MIN_USERNAME_LENGTH);
            when(userDAO.authenticateUser(exactlyMin, VALID_PASSWORD))
                    .thenReturn(new User(1, exactlyMin, UserType.MEMBER));

            ValidationResult result =
                    entryService.entryIsValid(exactlyMin, VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertTrue(result.valid());
        }

        @Test
        void usernameAtMaxLength_isAccepted() {
            String exactlyMax = "a".repeat(EntryService.MAX_USERNAME_LENGTH);
            when(userDAO.authenticateUser(exactlyMax, VALID_PASSWORD))
                    .thenReturn(new User(1, exactlyMax, UserType.MEMBER));

            ValidationResult result =
                    entryService.entryIsValid(exactlyMax, VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertTrue(result.valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {"bad user", "bad_user", "bad.user"})
        void usernameWithDisallowedCharacters_isRejected(String username) {
            ValidationResult result =
                    entryService.entryIsValid(username, VALID_PASSWORD, VALID_PASSWORD, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.USERNAME, result.type());
            verifyNoInteractions(userDAO);
        }
    }

    @Nested
    class PasswordValidation {

        @Test
        void nullPassword_isRejected() {
            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, null, null, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
        }

        @Test
        void emptyPassword_isRejected() {
            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, "", "", EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
            assertEquals("Please enter a password", result.message());
        }

        @Test
        void passwordBelowMinLength_isRejected() {
            String tooShort = "a".repeat(EntryService.MIN_PASSWORD_LENGTH - 1);

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, tooShort, tooShort, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
        }

        @Test
        void passwordAtMinLength_isAccepted() {
            String exactlyMin = "a".repeat(EntryService.MIN_PASSWORD_LENGTH);
            when(userDAO.authenticateUser(VALID_USERNAME, exactlyMin))
                    .thenReturn(new User(1, VALID_USERNAME, UserType.MEMBER));

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, exactlyMin, exactlyMin, EntryMode.SIGN_IN);

            assertTrue(result.valid());
        }

        @Test
        void passwordAboveMaxLength_isRejected() {
            String tooLong = "a".repeat(EntryService.MAX_PASSWORD_LENGTH + 1);

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, tooLong, tooLong, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
        }

        @ParameterizedTest
        @ValueSource(strings = {"bad password", "bad;password", "bad'password"})
        void passwordWithDisallowedCharacters_isRejected(String password) {
            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, password, password, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
        }
    }

    @Nested
    class SignIn {

        @Test
        void correctCredentials_returnsAuthenticatedUser() {
            User expectedUser = new User(42, VALID_USERNAME, UserType.MEMBER);
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).thenReturn(expectedUser);

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, VALID_PASSWORD, null, EntryMode.SIGN_IN);

            assertTrue(result.valid());
            assertEquals(expectedUser, result.user());
        }

        @Test
        void wrongCredentials_returnsDatabaseErrorWithMessage() {
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD))
                    .thenThrow(new InvalidCredentialsException());

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, VALID_PASSWORD, null, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.DATABASE, result.type());
            assertEquals("Invalid username or password", result.message());
        }

        @Test
        void databaseFailure_returnsDatabaseErrorWithUnderlyingMessage() {
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD))
                    .thenThrow(new DataAccessException("Failed to authenticate user", new RuntimeException("boom")));

            ValidationResult result =
                    entryService.entryIsValid(VALID_USERNAME, VALID_PASSWORD, null, EntryMode.SIGN_IN);

            assertFalse(result.valid());
            assertEquals(ErrorType.DATABASE, result.type());
            assertEquals("Failed to authenticate user", result.message());
        }

        @Test
        void confirmPasswordIsIgnored_inSignInMode() {
            User expectedUser = new User(1, VALID_USERNAME, UserType.MEMBER);
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).thenReturn(expectedUser);

            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, "completely different", EntryMode.SIGN_IN);

            assertTrue(result.valid());
            verify(userDAO, never()).registerUser(anyString(), anyString(), any());
        }
    }

    @Nested
    class CreateAccount {

        @Test
        void mismatchedConfirmPassword_isRejectedWithoutTouchingDao() {
            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, "somethingElse1!", EntryMode.CREATE_ACCOUNT);

            assertFalse(result.valid());
            assertEquals(ErrorType.PASSWORD, result.type());
            assertEquals("Passwords do not match", result.message());
            verifyNoInteractions(userDAO);
        }

        @Test
        void successfulRegistration_signsTheNewUserIn() {
            User newUser = new User(7, VALID_USERNAME, UserType.MEMBER);
            when(userDAO.registerUser(VALID_USERNAME, VALID_PASSWORD, UserType.MEMBER)).thenReturn(true);
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).thenReturn(newUser);

            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, EntryMode.CREATE_ACCOUNT);

            assertTrue(result.valid());
            assertEquals(newUser, result.user());
        }

        @Test
        void registrationReturnsFalse_isReportedAsUsernameTaken() {
            when(userDAO.registerUser(VALID_USERNAME, VALID_PASSWORD, UserType.MEMBER)).thenReturn(false);

            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, EntryMode.CREATE_ACCOUNT);

            assertFalse(result.valid());
            assertEquals(ErrorType.DATABASE, result.type());
            assertEquals("That username is already taken", result.message());
            verify(userDAO, never()).authenticateUser(anyString(), anyString());
        }

        @Test
        void registrationThrows_isReportedWithUnderlyingMessage() {
            when(userDAO.registerUser(VALID_USERNAME, VALID_PASSWORD, UserType.MEMBER))
                    .thenThrow(new DataAccessException("Failed to register user", new RuntimeException("boom")));

            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, EntryMode.CREATE_ACCOUNT);

            assertFalse(result.valid());
            assertEquals(ErrorType.DATABASE, result.type());
            assertEquals("Failed to register user", result.message());
        }

        @Test
        void authenticationAfterSuccessfulRegistrationFails_isReportedWithUnderlyingMessage() {
            when(userDAO.registerUser(VALID_USERNAME, VALID_PASSWORD, UserType.MEMBER)).thenReturn(true);
            when(userDAO.authenticateUser(VALID_USERNAME, VALID_PASSWORD))
                    .thenThrow(new InvalidCredentialsException());

            ValidationResult result = entryService.entryIsValid(
                    VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD, EntryMode.CREATE_ACCOUNT);

            assertFalse(result.valid());
            assertEquals(ErrorType.DATABASE, result.type());
            assertEquals("Invalid username or password", result.message());
        }
    }
}