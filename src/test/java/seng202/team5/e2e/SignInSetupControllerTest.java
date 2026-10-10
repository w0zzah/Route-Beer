package seng202.team5.e2e;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SignInSetupControllerTest extends FXTestBase {

    private String initialUsernameErrorMessage;
    private String initialPasswordErrorMessage;
    private String initialErrorMessage;

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        robot.interact(() -> screenNavigator.goToSetup(false)); // go to sign in
        initialUsernameErrorMessage = getUsernameErrorMessage(robot);
        initialPasswordErrorMessage = getPasswordErrorMessage(robot);
        initialErrorMessage = getErrorMessage(robot);
    }

    @Test
    void signInEmptyFields(FxRobot robot) {
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialUsernameErrorMessage, getUsernameErrorMessage(robot));
    }

    @Test
    void signInUsernameOnly(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialPasswordErrorMessage, getPasswordErrorMessage(robot));
    }

    @Test
    void signInPasswordOnly(FxRobot robot) {
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertEquals("Please enter a username", getUsernameErrorMessage(robot));
    }

    @Test
    void signInShortUsername(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("u"); // less than minimum 5 characters
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialUsernameErrorMessage, getUsernameErrorMessage(robot));
    }

    @Test
    void signInLongUsername(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("usernameUsername1"); // greater than maximum 16 characters
        assertEquals("usernameUsername", getUsernameField(robot));
    }

    @Test
    void signInShortPassword(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("p");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialPasswordErrorMessage, getPasswordErrorMessage(robot));
    }

    @Test
    void signInLongPassword(FxRobot robot) {
        robot.clickOn("#passwordField");
        robot.write("passwordPassword1");
        assertEquals("passwordPassword", getPasswordField(robot));
    }

    @Test
    void signInIncorrectUsernameAndPassword(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username33333333");
        robot.clickOn("#passwordField");
        robot.write("password33333333");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialErrorMessage, getErrorMessage(robot));
    }

    @Test
    void signInCorrectUsernameIncorrectPassword(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("password33333333");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialErrorMessage, getErrorMessage(robot));
    }

    @Test
    void signInCorrectUsernameAndPassword(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("mainWindowStackPane", stage.getScene().getRoot().getId());
    }

    private String getUsernameErrorMessage(FxRobot robot) {
        return robot.lookup("#usernameErrorLabel").queryLabeled().getText();
    }

    private String getPasswordErrorMessage(FxRobot robot) {
        return robot.lookup("#passwordErrorLabel").queryLabeled().getText();
    }

    private String getErrorMessage(FxRobot robot) {
        return robot.lookup("#errorLabel").queryLabeled().getText();
    }

    private String getUsernameField(FxRobot robot) {
        return robot.lookup("#usernameField").queryAs(TextField.class).getText();
    }

    private String getPasswordField(FxRobot robot) {
        return robot.lookup("#passwordField").queryAs(TextField.class).getText();
    }
}