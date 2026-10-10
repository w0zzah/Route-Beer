package seng202.team5.e2e;

import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import seng202.team5.models.User;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.NodeQueryUtils.hasText;
import static seng202.team5.models.types.UserType.MEMBER;

class GenerateRouteTest extends FXTestBase {

    User testUser;

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    public void setup(FxRobot robot) {
        testUser = new User(0, "user1", MEMBER);
        robot.interact(() -> screenNavigator.goToMain(testUser));
    }

    @Test
    public void testGenerateRouteStartBarOnly(FxRobot robot) {
        robot.clickOn("#startBarField");
        robot.write("Fat");

        robot.sleep(200);
        robot.clickOn("Fat Eddie's");
        robot.clickOn("#generateRouteButton");

        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("viewTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertEquals("Fat Eddie's", robot.lookup("#barCardName").queryAs(Label.class).getText()); // gets the first card
    }

    @Test
    public void testGenerateRouteNoStartBar(FxRobot robot) {
        robot.clickOn("#generateRouteButton");

        assertEquals("routeTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertFalse(robot.lookup("#startBarErrorLabel").queryAs(Label.class).getText().isEmpty());
    }

    @Test
    public void testGenerateRouteInvalidStartBar(FxRobot robot) {
        robot.clickOn("#startBarField");
        robot.write("Invalid");
        robot.clickOn("#generateRouteButton");

        assertEquals("routeTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertFalse(robot.lookup("#startBarErrorLabel").queryAs(Label.class).getText().isEmpty());
    }

    @Test
    void testGenerateRouteInvalidEndBar(FxRobot robot) {
        robot.clickOn("#startBarField");
        robot.write("Fat Eddie's");
        robot.clickOn("#endBarField");
        robot.write("Invalid");
        robot.clickOn("#generateRouteButton");

        assertEquals("routeTab", robot.lookup("#mainTabPane").queryAs(TabPane.class).getSelectionModel().getSelectedItem().getId());
        assertFalse(robot.lookup("#endBarErrorLabel").queryAs(Label.class).getText().isEmpty());
    }

    @Test
    void testGenerateRouteAllParameters(FxRobot robot) {
        robot.clickOn("#startBarField");
        robot.write("The Crai");
        robot.sleep(200);
        robot.clickOn("The Craic");

        robot.clickOn("#endBarField");
        robot.write("Fat ");
        robot.sleep(200);
        robot.clickOn("Fat Eddie's");

        robot.clickOn("#approxDistanceField");
        robot.write("5");

        robot.clickOn("#numBarsSpinner");
        robot.write("6");

        robot.clickOn("#barFeaturesCheckComboBox");
        robot.clickOn("Pool Table");
        robot.clickOn("#barFeaturesCheckComboBox");

        robot.clickOn("#barThemesCheckComboBox");
        robot.clickOn("Irish Pub");
        robot.clickOn("#barThemesCheckComboBox");

        robot.clickOn("#particularBarsSearchField");
        robot.write("Pegasus Arm");
        robot.sleep(200);
        robot.clickOn("Pegasus Arms");
        robot.clickOn("#addParticularBarButton");

        robot.clickOn("#generateRouteButton");

    }

}
