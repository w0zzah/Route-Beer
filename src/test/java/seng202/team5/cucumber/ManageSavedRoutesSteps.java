package seng202.team5.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import seng202.team5.exceptions.InvalidCredentialsException;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.MainManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManageSavedRoutesSteps {

    private MainManager mainManager = new MainManager();
    private User user;
    private Route selectedRoute;
    private String username = "cucumbertester";
    private String password = "passw0rd!";
    private List<Route> routes = new ArrayList<>();

    private void getTestUser() {
        try {
            user = mainManager.userDAO.authenticateUser(username, password);
        } catch (InvalidCredentialsException e) {
            mainManager.userDAO.registerUser(username, password, UserType.MEMBER);
            user = mainManager.userDAO.authenticateUser(username, password);
        }
    }

    private void clearRoutes() {
        getTestUser();
        try (java.sql.Connection connection = seng202.team5.data.DatabaseConnection.getConnection();
             java.sql.PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM routes WHERE user_id = ?")) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void signInWithRoutes(int numRoutes) {
        getTestUser();
        mainManager.setCurrentUser(user);
        for (int i = 1; i <= numRoutes; i++) {
            Route route = new Route(new ArrayList<>(mainManager.getBars().subList(0, 3)));
            route.setName("Route " + i);
            mainManager.routeDAO.saveRoute(route, user.getId());
        }
        mainManager.setUserRoutes();
    }

    @Before("@AT_5 or @AT_6 or @AT_22")
    public void setup() {
        clearRoutes();
    }

    // AT_5

    @Given("A user is signed in with one or more saved routes")
    public void aUserIsSignedInWithOneOrMoreSavedRoutes() {
        signInWithRoutes(3);
    }

    @When("The user selects 'Saved Routes'")
    public void theUserSelectsSavedRoutes() {
        routes = mainManager.getUserRoutes();
    }

    @Then("Each route associated with the account is shown")
    public void eachRouteAssociatedWithTheAccountIsShown() {
        assertEquals(3, routes.size());
        List<String> names = mainManager.getSavedRouteNames();
        assertTrue(names.contains("Route 1"));
        assertTrue(names.contains("Route 2"));
        assertTrue(names.contains("Route 3"));
    }

    // AT_6

    @Given("A user is signed in")
    public void aUserIsSignedIn() {
        getTestUser();
        mainManager.setCurrentUser(user);
    }

    @Given("The user is on the 'Saved Routes' tab")
    public void theUserIsOnTheSavedRoutesTab() {
        signInWithRoutes(1);
        routes = mainManager.getUserRoutes();
    }

    @When("The user selects the 'Load' button")
    public void theUserSelectsTheLoadButton() {
        selectedRoute = routes.getFirst();
        mainManager.setCurrentRoute(selectedRoute);
    }

    @Then("The route is generated onto the map")
    public void theRouteIsGeneratedOntoTheMap() {
        Route current = mainManager.getCurrentRoute();
        assertNotNull(current);
        assertEquals(selectedRoute.getBars(), current.getBars());
    }

    @Then("The route details are displayed in the 'Route Details' tab")
    public void theRouteDetailsAreDisplayedInTheRouteDetailsTab() {
        Route current = mainManager.getCurrentRoute();
        assertEquals("Route 1", current.getName());
        assertEquals(3, current.getNumStops());
        assertTrue(current.getTotalDistanceKm() > 0);
    }

    // AT_22

    @Given("The user has a saved route in 'Saved Routes'")
    public void theUserHasASavedRouteInSavedRoutes() {
        signInWithRoutes(1);
        routes = mainManager.getUserRoutes();
        assertEquals(1, routes.size());
    }

    @When("the user selects 'Delete' on the route card and confirms")
    public void theUserSelectsDeleteOnTheRouteCardAndConfirms() {
        Route route = routes.getFirst();
        try (java.sql.Connection connection = seng202.team5.data.DatabaseConnection.getConnection();
             java.sql.PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM routes WHERE user_id = ?")) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
        mainManager.setUserRoutes();
    }

    @Then("The route is removed from their saved routes and cannot be retrieved")
    public void theRouteIsRemovedFromTheirSavedRoutesAndCannotBeRetrieved() {
        assertFalse(mainManager.getSavedRouteNames().contains("Route 1"));
        assertEquals(0, mainManager.getUserRoutes().size());
    }

    @After("@AT_5 or @AT_6 or @AT_22")
    public void tearDown() {
        clearRoutes();
    }
}