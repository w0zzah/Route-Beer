package seng202.team5.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.Before;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.MainManager;

import static org.junit.jupiter.api.Assertions.*;

public class CreateAccountSteps {

    private MainManager mainManager = new MainManager();
    private String username = "cucumbertester1";
    private String password = "passw0rd!";
    private boolean registered;
    private DataAccessException exception;
    private DataAccessException exception2;

    private void deleteUser() {
        try {
            mainManager.userDAO.deleteUser(username);
        } catch (Exception e) {
            // user did not exist
        }
    }


    @Given("The user is on the create account window")
    public void theUserIsOnTheCreateAccountWindow() {
        // No action needed
    }

    @Given("Has a valid username and password entered")
    public void hasAValidUsernameAndPasswordEntered() {
        // username / password fixed
    }

    @Given("Has a username inputted which is already registered")
    public void hasAUsernameInputtedWhichIsAlreadyRegistered() {
        try {
            mainManager.userDAO.registerUser("newuser21", password, UserType.MEMBER);
        } catch (DataAccessException e) {
            exception2 = e;
        }
    }

    @When("The user selects 'Create Account'")
    public void theUserSelectsCreateAccount() {
        try {
            registered = mainManager.userDAO.registerUser(username, password, UserType.MEMBER);
        } catch (DataAccessException e) {
            exception = e;
        }
    }

    @Then("The account becomes registered in the database")
    public void theAccountBecomesRegisteredInTheDatabase() {
        assertTrue(registered);
        assertNull(exception);
        User user = mainManager.userDAO.authenticateUser(username, password);
        assertEquals(UserType.MEMBER, user.getType());
    }

    @Then("The user is directed to the main window")
    public void theUserIsDirectedToTheMainWindow() {
        User user = mainManager.userDAO.authenticateUser(username, password);
        assertEquals(UserType.MEMBER, user.getType());
    }

    @Then("The user is notified that the username is already taken")
    public void theUserIsNotifiedThatTheUsernameIsAlreadyTaken() {
        assertNotNull(exception2);
    }

    @After("AT_7 or @AT_20")
    public void tearDown() {
        deleteUser();
    }
}
