package seng202.team5.gui.entry;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import seng202.team5.gui.ScreenNavigator;
import seng202.team5.models.types.EntryMode;
import seng202.team5.models.types.ErrorType;
import seng202.team5.models.User;
import seng202.team5.models.types.UserType;
import seng202.team5.services.entry.EntryService;
import seng202.team5.services.entry.ValidationResult;

import java.util.function.UnaryOperator;

/**
 * Controller for the sign in / create account entry screen.
 */
public class EntryController {

    private static final String USERNAME_HINT =
            "(" + EntryService.MIN_USERNAME_LENGTH + "-" + EntryService.MAX_USERNAME_LENGTH + " characters)";
    private static final String PASSWORD_HINT =
            "(" + EntryService.MIN_PASSWORD_LENGTH + "-" + EntryService.MAX_PASSWORD_LENGTH + " characters)";
    private static final String ERROR_STYLE = "-fx-text-fill: red;";
    private static final String FIELD_ERROR_STYLE = "-fx-border-color: red;";

    @FXML
    private ToggleButton createButton;
    @FXML
    private ToggleButton signInButton;
    @FXML
    private Button continueButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;

    private final ObjectProperty<EntryMode> mode = new SimpleObjectProperty<>(EntryMode.SIGN_IN);

    private EntryService entryService;
    private ScreenNavigator navigator;

    /**
     * Initialises the controller.
     *
     * @param stage       the primary stage
     * @param navigator   the screen navigator used to move between screens
     * @param accountPage true to open directly on the "create account" tab
     */
    public void initialise(Stage stage, ScreenNavigator navigator, boolean accountPage) {
        this.entryService = new EntryService();
        this.navigator = navigator;

        setCharRestrictions();

        if (accountPage) {
            createButton.setSelected(true);
            setMode(EntryMode.CREATE_ACCOUNT);
        } else {
            setMode(EntryMode.SIGN_IN);
        }
    }

    @FXML
    private void onSignInButtonClicked() {
        setMode(EntryMode.SIGN_IN);
    }

    @FXML
    private void onCreateButtonClicked() {
        setMode(EntryMode.CREATE_ACCOUNT);
    }

    /**
     * Loads elements needed for the given setup screen mode
     * @param newMode the given mode
     */
    private void setMode(EntryMode newMode) {
        mode.set(newMode);
        boolean creatingAccount = newMode == EntryMode.CREATE_ACCOUNT;

        createButton.setSelected(creatingAccount);
        signInButton.setSelected(!creatingAccount);

        confirmPasswordField.setVisible(creatingAccount);
        confirmPasswordLabel.setVisible(creatingAccount);
        continueButton.setText(creatingAccount ? "Create New Account!" : "Sign In!");

        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        clearErrors();
    }

    /**
     * Validates the user's inputs and continues to main window if valid
     * otherwise shows error messages
     */
    @FXML
    private void onContinueClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmedPassword = confirmPasswordField.getText();

        clearErrors();

        ValidationResult result = entryService.entryIsValid(username, password, confirmedPassword, mode.getValue());
        if (result.valid()) {
            navigator.goToMain(result.user());
        } else {
            showError(result.type(), result.message());
        }
    }

    @FXML
    private void onGuestButtonClicked() {
        User currentUser = new User(-1, "Guest", UserType.GUEST);
        navigator.goToMain(currentUser);
    }

    /**
     * Displays an error appropriate to its type: USERNAME/PASSWORD errors go
     * on the small inline label next to the relevant field and mark that
     * field's border red; DATABASE/GENERAL errors (e.g. wrong credentials,
     * a DB failure, a taken username) go on the general error label at the
     * bottom. Either way, the affected label glows red a few times to draw
     * the eye before settling into the steady red state.
     */
    private void showError(ErrorType type, String message) {
        switch (type) {
            case USERNAME -> {
                usernameErrorLabel.setText(message);
                usernameErrorLabel.setStyle(ERROR_STYLE);
                usernameField.setStyle(FIELD_ERROR_STYLE);
                glowRed(usernameErrorLabel);
            }
            case PASSWORD -> {
                passwordErrorLabel.setText(message);
                passwordErrorLabel.setStyle(ERROR_STYLE);
                passwordField.setStyle(FIELD_ERROR_STYLE);
                glowRed(passwordErrorLabel);
            }
            case DATABASE, GENERAL -> {
                errorLabel.setText(message);
                errorLabel.setStyle(ERROR_STYLE);
                glowRed(errorLabel);
            }
        }
    }

    /**
     * Resets every error label and field back to its default, non-error
     * state. Called before each validation attempt and whenever the mode
     * (sign in / create account) changes, so stale errors from a previous
     * attempt never linger on screen.
     */
    private void clearErrors() {
        errorLabel.setText("");
        errorLabel.setStyle(null);

        usernameErrorLabel.setText(USERNAME_HINT);
        usernameErrorLabel.setStyle(null);
        usernameField.setStyle(null);

        passwordErrorLabel.setText(PASSWORD_HINT);
        passwordErrorLabel.setStyle(null);
        passwordField.setStyle(null);
        confirmPasswordField.setStyle(null);
    }

    /**
     * Pulses a red glow on the given node a few times, then lets it fade
     * out - the node's own red text/border (set separately via CSS style)
     * remains in place after the animation finishes.
     */
    private void glowRed(Node node) {
        DropShadow glow = new DropShadow(20, Color.RED);
        glow.setSpread(0.3);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, e -> node.setEffect(glow)),
                new KeyFrame(Duration.millis(270), e -> node.setEffect(null)),
                new KeyFrame(Duration.millis(540), e -> node.setEffect(glow)),
                new KeyFrame(Duration.millis(810), e -> node.setEffect(null))
        );
        pulse.play();
    }

    /**
     * Sets the character restrictions
     */
    private void setCharRestrictions() {
        usernameField.setTextFormatter(new TextFormatter<>(charLimitFilter(EntryService.MAX_USERNAME_LENGTH)));
        passwordField.setTextFormatter(new TextFormatter<>(charLimitFilter(EntryService.MAX_PASSWORD_LENGTH)));
        confirmPasswordField.setTextFormatter(new TextFormatter<>(charLimitFilter(EntryService.MAX_PASSWORD_LENGTH)));
    }

    private UnaryOperator<TextFormatter.Change> charLimitFilter(int maxLength) {
        return change -> change.getControlNewText().length() > maxLength ? null : change;
    }
}