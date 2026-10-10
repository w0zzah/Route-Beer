package seng202.team5.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.After;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.Route;
import seng202.team5.models.User;

import seng202.team5.models.types.UserType;
import seng202.team5.services.MainManager;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SaveRouteSteps {

    private MainManager mainManager = new MainManager();
    private String username = "cucumbertester";
    private String password = "passw0rd!";
    private User user;
    private boolean saved;
    private Route route;

    private void getTestUser() {
        user = mainManager.userDAO.authenticateUser(username, password);
        mainManager.setCurrentUser(user);
    }

    private void clearRoutes() {
        try (java.sql.Connection connection = seng202.team5.data.DatabaseConnection.getConnection();
             java.sql.PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM routes WHERE user_id = ?")) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Given("The user is logged in")
    public void theUserIsLoggedIn() {
        getTestUser();
        clearRoutes();
        mainManager.setCurrentUser(user);
    }

    @Given("A route has been created on the home screen")
    public void aRouteHasBeenCreatedOnTheHomeScreen() {
        route = new Route(new ArrayList<>(mainManager.getBars().subList(0, 3)));
        route.setName("Test Route");
        mainManager.setCurrentRoute(route);
    }

    @When("The user selects Save Route")
    public void theUserSelectsSaveRoute() {
        saved = mainManager.routeDAO.saveRoute(mainManager.getCurrentRoute(), user.getId());
        mainManager.setUserRoutes();
    }

    @Then("The route is added to their saved routes")
    public void theRouteIsAddedToTheirSavedRoutes() {
        assertTrue(saved);
        assertTrue(mainManager.getSavedRouteNames().contains("Test Route"));
    }

    @After("@AT_3")
    public void tearDown() {
        clearRoutes();
    }
}
