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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seng202.team5.models.types.UserType.MEMBER;

class MemberTabPaneNavigationTest extends FXTestBase {

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        robot.interact(() -> screenNavigator.goToMain(testUser));
    }

    @Test
    void clickSavedRoutes(FxRobot robot) {
        robot.clickOn("#savedTabButton");
        assertTrue(robot.lookup("#savedTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("savedTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertNull(stage.getScene().getRoot().lookup("#signInSavedButton"));
    }

    @Test
    void clickPublicRoutes(FxRobot robot) {
        robot.clickOn("#publicTabButton");
        assertTrue(robot.lookup("#publicTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("communityTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        robot.clickOn("#upvoteButton");
        assertTrue(robot.lookup("#upvoteButton").queryAs(ToggleButton.class).isSelected());
    }

    @Test
    void clickRouteMaker(FxRobot robot) {
        robot.clickOn("#builderTabButton");
        assertTrue(robot.lookup("#builderTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("routeTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertNotNull(robot.lookup("#routeDetailsTabVBox"));

    }

    @Test
    void clickRouteDetails(FxRobot robot) {
        robot.clickOn("#detailsTabButton");
        assertTrue(robot.lookup("#detailsTabButton").queryAs(ToggleButton.class).isSelected());
        assertEquals("viewTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
    }
}
