package seng202.team5.unittests.services;

import org.junit.jupiter.api.Test;

import seng202.team5.models.ErrorType;
import seng202.team5.models.User;
import seng202.team5.models.UserType;
import seng202.team5.services.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultTest {

    @Test
    void ok_carriesTheGivenUserAndNoError() {
        User user = new User(1, "someone", UserType.MEMBER);

        ValidationResult result = ValidationResult.ok(user);

        assertTrue(result.valid());
        assertEquals(user, result.user());
        assertNull(result.type());
        assertNull(result.message());
    }

    @Test
    void error_carriesTheGivenTypeAndMessageWithNoUser() {
        ValidationResult result = ValidationResult.error(ErrorType.USERNAME, "bad username");

        assertFalse(result.valid());
        assertNull(result.user());
        assertEquals(ErrorType.USERNAME, result.type());
        assertEquals("bad username", result.message());
    }
}