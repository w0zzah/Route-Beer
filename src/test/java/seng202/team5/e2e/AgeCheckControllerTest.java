package seng202.team5.e2e;

import javafx.scene.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AgeCheckControllerTest extends FXTestBase {

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    public void setup(FxRobot robot) {
        robot.interact(() -> screenNavigator.goToAgeCheck());
    }

    @Test
    void ageCheckNoTest(FxRobot robot) {
        System.out.println("Root id: " + stage.getScene().getRoot().getId());
        System.out.println("Scene graph:");
        printNodeTree(stage.getScene().getRoot(), 0);

        robot.clickOn("#noButton");
        robot.clickOn("#noButton"); // Click to Exit button reuses #noButton
        assertFalse(stage.isShowing());
    }

    @Test
    void ageCheckYesTest(FxRobot robot) {
        robot.clickOn("#yesButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
    }

    // helper
    private void printNodeTree(Node node, int depth) {
        System.out.println("  ".repeat(depth) + node.getClass().getSimpleName()
                + " id=" + node.getId());
        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                printNodeTree(child, depth + 1);
            }
        }
    }
}
