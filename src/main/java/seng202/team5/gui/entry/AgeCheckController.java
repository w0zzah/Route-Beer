package seng202.team5.gui.entry;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import seng202.team5.gui.ScreenNavigator;

/**
 * Controller for the Age Check window
 */
public class AgeCheckController {

    @FXML
    private HBox ageCheckHBox;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;

    private ScreenNavigator navigator;
    public Label infoLabel;

    /**
     * Initialises the controller
     * @param stage The current stage
     * @param navigator The screen navigator
     */
    public void initialise(Stage stage, ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Launch setup page
     */
    @FXML
    private void onYesButtonClicked() {
        navigator.goToSetup(false);
    }

    /**
     * Shows an age restriction message and closes the app after a countdown
     */
    @FXML
    private void onNoButtonClicked() {
        yesButton.setVisible(false);
        yesButton.setManaged(false);
        noButton.setText("Click to Exit");
        ageCheckHBox.getChildren();
        infoLabel.setText("Sorry you must be over the age of 18 to use this application\n");
        noButton.setOnAction(e -> ((Stage) noButton.getScene().getWindow()).close());
    }
}