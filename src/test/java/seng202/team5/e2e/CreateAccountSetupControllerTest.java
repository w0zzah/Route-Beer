package seng202.team5.e2e;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import seng202.team5.data.UserDAO;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.models.types.UserType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class CreateAccountSetupControllerTest extends FXTestBase {

    private String initialUsernameErrorMessage;
    private String initialPasswordErrorMessage;
    private String initialErrorMessage;

    @Start
    public void start(Stage stage) {
        startSetup(stage);
    }

    @BeforeEach
    void setup(FxRobot robot) {
        robot.interact(() -> screenNavigator.goToSetup(true)); // go to create account
        initialUsernameErrorMessage = getUsernameErrorMessage(robot);
        initialPasswordErrorMessage = getPasswordErrorMessage(robot);
        initialErrorMessage = getErrorMessage(robot);
    }

    @Test
    void createAccountEmptyFields(FxRobot robot) {
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialUsernameErrorMessage, getUsernameErrorMessage(robot));
    }

    @Test
    void createAccountUsernameOnly(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialPasswordErrorMessage, getPasswordErrorMessage(robot));
    }

    @Test
    void createAccountPasswordAndConfirmPasswordOnly(FxRobot robot) {
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#confirmPasswordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialUsernameErrorMessage, getUsernameErrorMessage(robot));
    }

    @Test
    void createAccountShortUsername(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("u");
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#confirmPasswordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialUsernameErrorMessage, getUsernameErrorMessage(robot));
    }

    @Test
    void createAccountLongUsername(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("usernameUsername1");
        assertEquals("usernameUsername", getUsernameField(robot));
    }

    @Test
    void createAccountShortPassword(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("p");
        robot.clickOn("#confirmPasswordField");
        robot.write("p");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialPasswordErrorMessage, getPasswordErrorMessage(robot));
    }

    @Test
    void createAccountLongPassword(FxRobot robot) {
        robot.clickOn("#passwordField");
        robot.write("passwordPassword1");
        robot.clickOn("#confirmPasswordField");
        robot.write("passwordPassword1");
        assertEquals("passwordPassword", getPasswordField(robot));
    }

    @Test
    void createAccountPasswordsDontMatch(FxRobot robot) {
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("password1");
        robot.clickOn("#confirmPasswordField");
        robot.write("password2");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialPasswordErrorMessage, getPasswordErrorMessage(robot));
    }

    @Test
    void createAccountAlreadyExists(FxRobot robot) {
        UserDAO userDAO = new UserDAO();
        try {
            userDAO.registerUser("username", "password", UserType.MEMBER);
        } catch (DataAccessException _) {
            // tries to add user to ensure that user already exists when testing for this below
        }
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#confirmPasswordField");
        robot.write("password");
        robot.clickOn("#continueButton");
        assertEquals("setupStackPane", stage.getScene().getRoot().getId());
        assertNotEquals(initialErrorMessage, getErrorMessage(robot));
    }

    @Test
    void createAccountSuccess(FxRobot robot) {
        UserDAO userDAO = new UserDAO();
        userDAO.deleteUser("username");
        robot.clickOn("#usernameField");
        robot.write("username");
        robot.clickOn("#passwordField");
        robot.write("password");
        robot.clickOn("#confirmPasswordField");
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