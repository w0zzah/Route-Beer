package seng202.team5.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.services.MainManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SignInSteps {

    private MainManager mainManager = new MainManager();
    private final String username = "cucumbertester";
    private final String password = "passw0rd!";
    private User registered;
    private User signedInUser;
    private DataAccessException exception;
    private InvalidCredentialsException invalidUsername;
    private InvalidCredentialsException invalidPassword;

    private void clearRoutes() {
        for (Route route : mainManager.routeDAO.getUserRoutes(registered.getId())) {
            try (java.sql.Connection connection = seng202.team5.data.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement statement =
                         connection.prepareStatement("DELETE FROM routes WHERE user_id = ?")) {
                statement.setInt(1, registered.getId());
                statement.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Given("A user is on the login window")
    public void aUserIsOnTheLoginWindow() {
        registered = mainManager.userDAO.authenticateUser(username, password);
        clearRoutes();
        Route route = new Route(new ArrayList<>(mainManager.getBars().subList(0, 3)));
        route.setName("Test Route");
        mainManager.routeDAO.saveRoute(route, registered.getId());
    }

    @When("The user selects sign in with a registered username")
    public void theUserSelectsSignInWithARegisteredUsername() {
        signedInUser = mainManager.userDAO.authenticateUser(username, password);
    }

    @When("The user selects sign in with an invalid username or password")
    public void theUserSelectsSignInWithAnInvalidUsernameOrPassword() {
        try {
            mainManager.userDAO.authenticateUser("Username", password);
        } catch (InvalidCredentialsException e) {
            invalidUsername = e;
        }
        try {
            mainManager.userDAO.authenticateUser(username, "WrongPWord");
        } catch (InvalidCredentialsException e) {
            invalidPassword = e;
        }
    }

    @Then("The user will load into the main window of the application")
    public void theUserWillLoadIntoTheMainWindowOfTheApplication() {
        assertNull(invalidUsername);
        assertNull(invalidPassword);
        assertEquals(username, signedInUser.getUsername());
    }

    @Then("The data associated with the user will be loaded")
    public void theDataAssociatedWithTheUserWillBeLoaded() {
        mainManager.setCurrentUser(signedInUser);
        mainManager.setUserRoutes();
        assertTrue(mainManager.getSavedRouteNames().contains("Test Route"));

    }

    @Then("The user is notified that they have inputted an invalid username or password")
    public void theUserIsNotifiedThatTheyHaveInputtedAnInvalidUsernameOrPassword() {
        assertNotNull(invalidUsername);
        assertNotNull(invalidPassword);

    }

    @After("@AT_8 or @AT_15")
    public void clean() {
        clearRoutes();
    }
}
