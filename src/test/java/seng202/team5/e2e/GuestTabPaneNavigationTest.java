package seng202.team5.e2e;

import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import seng202.team5.models.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seng202.team5.models.types.UserType.GUEST;

class GuestTabPaneNavigationTest extends FXTestBase {

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        testUser = new User(0, "guest1", GUEST);
        robot.interact(() -> screenNavigator.goToMain(testUser));
    }

    @Test
    void clickSavedRoutes(FxRobot robot) {
        robot.clickOn("#savedTabButton");
        assertTrue(robot.lookup("#savedTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("savedTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertNotNull(stage.getScene().getRoot().lookup("#signInSavedButton"));
    }

    @Test
    void clickPublicRoutes(FxRobot robot) {
        robot.clickOn("#publicTabButton");
        assertTrue(robot.lookup("#publicTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("communityTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        robot.clickOn("#upvoteButton");
        robot.sleep(3000);
        assertFalse(robot.lookup("#upvoteButton").queryAs(ToggleButton.class).isSelected());
    }

}