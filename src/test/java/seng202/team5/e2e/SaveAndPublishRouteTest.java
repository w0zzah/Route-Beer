package seng202.team5.e2e;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import seng202.team5.data.RouteDAO;
import seng202.team5.data.UserDAO;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class SaveAndPublishRouteTest extends FXTestBase {

    // Satisfies EntryService validation (5-16 chars, no spaces/"_"/"."; password 8-16 chars)
    private static final String TEST_USERNAME = "e2eroutetest";
    private static final String TEST_PASSWORD = "TestPass123";

    private RouteDAO routeDAO;
    private User routeUser;

    @Start
    void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        routeUser = getOrCreateTestUser();
        routeDAO = new RouteDAO(mainManager);

        deleteAllRoutesFor(routeUser);
        routeDAO.saveRoute(
                new Route(0, "testRoute0", "", mainManager.locationManager.getBars().subList(0, 3),
                        LocalDate.now(), routeUser.getUsername()),
                routeUser.getId());

        robot.interact(() -> screenNavigator.goToMain(routeUser));
        robot.clickOn("#startBarField");
        robot.write("The Crai");
        robot.sleep(200);
        robot.clickOn("The Craic");
        robot.clickOn("#generateRouteButton");
    }

    @AfterEach
    void cleanup() {
        if (routeUser != null) {
            deleteAllRoutesFor(routeUser);
        }
    }

    @Test
    void saveRouteNoName(FxRobot robot) {
        assertTrue(robot.lookup("#saveRouteButton").query().isDisabled());
        robot.clickOn("#saveRouteButton");
        assertTrue(robot.lookup("#saveRouteButton").query().isDisabled());
    }

    @Test
    void saveRouteNameAlreadyExists(FxRobot robot) {
        robot.clickOn("#routeNameField");
        robot.write("testRoute0");
        assertFalse(robot.lookup("#routeNameErrorLabel").queryLabeled().getText().isEmpty());
        robot.clickOn("#saveRouteButton");

        assertFalse(robot.lookup("#routeNameErrorLabel").queryLabeled().getText().isEmpty());
        assertEquals("viewTab", selectedTabId(robot));
    }

    @Test
    void saveRoute(FxRobot robot) {
        robot.clickOn("#routeNameField");
        robot.write("testRoute1");
        robot.clickOn("#routeDescriptionField");
        robot.write("testRoute1 description");
        robot.clickOn("#saveRouteButton");

        DialogPane dialogPane = findDialogPane(robot);
        assertEquals("Route Successfully Saved!", dialogPane.getContentText());

        robot.interact(((Button) dialogPane.lookupButton(ButtonType.OK))::fire);
        assertEquals("savedTab", selectedTabId(robot));
    }

    @Test
    void publishRoute(FxRobot robot) {
        robot.clickOn("#savedTabButton");
        robot.clickOn("#cardPublishButton");

        DialogPane dialogPane = findDialogPane(robot);
        assertEquals("Route Successfully Published!", dialogPane.getContentText());
        robot.interact(((Button) dialogPane.lookupButton(ButtonType.OK))::fire);

        robot.clickOn("#publicTabButton");
        assertFalse(robot.lookup("#cardNameLabel").match(hasText("testRoute0")).queryAll().isEmpty());
    }

    @Test
    void loadSavedRoute(FxRobot robot) {
        robot.clickOn("#savedTabButton");
        robot.clickOn("#loadRouteButton");
        assertEquals("viewTab", selectedTabId(robot));
    }

    @Test
    void loadPublicRoute(FxRobot robot) {
        robot.clickOn("#publicTabButton");
        robot.clickOn("#loadRouteButton");
        assertEquals("viewTab", selectedTabId(robot));
    }

    private User getOrCreateTestUser() {
        UserDAO userDAO = new UserDAO();
        userDAO.registerUser(TEST_USERNAME, TEST_PASSWORD, UserType.MEMBER); // false if it already exists, which is fine
        return userDAO.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
    }

    private void deleteAllRoutesFor(User user) {
        for (Route route : routeDAO.getUserRoutes(user.getId())) {
            routeDAO.deleteSavedRoute(route.getRouteID());
        }
    }

    private String selectedTabId(FxRobot robot) {
        return robot.lookup("#mainTabPane").queryAs(TabPane.class)
                .getSelectionModel().getSelectedItem().getId();
    }

    private DialogPane findDialogPane(FxRobot robot) {
        DialogPane dialogPane = (DialogPane) robot.window(w -> w.isShowing()
                && w.getScene() != null
                && w.getScene().getRoot() instanceof DialogPane).getScene().getRoot();
        assertNotNull(dialogPane);
        return dialogPane;
    }
}