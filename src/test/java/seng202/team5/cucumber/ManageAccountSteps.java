package seng202.team5.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.AccountService;
import seng202.team5.services.MainManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ManageAccountSteps {

    private MainManager mainManager = new MainManager();
    private User user;
    private final String username = "cucumber_delete_user";
    private final String password = "Passw0rd!";

    private void getTestUser() {
        try {
            user = mainManager.userDAO.authenticateUser(username, password);
        } catch (InvalidCredentialsException e) {
            mainManager.userDAO.registerUser(username, password, UserType.MEMBER);
            user = mainManager.userDAO.authenticateUser(username, password);
        }
    }

    @Given("The user is logged in with a saved route")
    public void theUserIsLoggedInWithASavedRoute() {
        getTestUser();
        mainManager.setCurrentUser(user);

        Route route = new Route(new ArrayList<>(mainManager.getBars().subList(0, 3)));
        route.setName("Account Route");
        mainManager.routeDAO.saveRoute(route, user.getId());
    }

    @When("The user selects 'Log Out'")
    public void theUserSelectsLogOut() {
        mainManager.setCurrentUser(null);
        mainManager.setCurrentRoute(null);
    }

    @Then("The user is directed back to the login page")
    public void theUserIsDirectedBackToTheLoginPage() {
        assertNull(mainManager.getCurrentUser());
        assertNull(mainManager.getCurrentRoute());
    }

    @When("The user selects 'Delete Account' and confirms")
    public void theUserSelectsDeleteAccountAndConfirms() {
        boolean deleted = mainManager.userDAO.deleteUser(user.getUsername());
        assertTrue(deleted, "deleteUser removed no rows for " + user.getUsername());
    }

    @Then("The user is taken back to the login page")
    public void theUserIsTakenBackToTheLoginPage() {
        assertThrows(InvalidCredentialsException.class,
                () -> mainManager.userDAO.authenticateUser(username, password));
    }

    @Then("All user associated data is deleted")
    public void allUserAssociatedDataIsDeleted() {
        assertTrue(mainManager.routeDAO.getUserRoutes(user.getId()).isEmpty());
    }

    @After("@AT_21")
    public void tearDown() {
        try {
            mainManager.userDAO.deleteUser(username);
        } catch (Exception e) {
            // already deleted by the test
        }
    }
}