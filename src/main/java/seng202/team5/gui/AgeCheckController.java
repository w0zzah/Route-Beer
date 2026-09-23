package seng202.team5.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Controller for the Age Check window
 */
public class AgeCheckController {

    @FXML
    private HBox ageCheckHBox;

    private ScreenNavigator navigator;
    public Label infoLabel;

    private static final int COUNTDOWN_START = 3;

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
        ageCheckHBox.getChildren().clear();
        int[] secondsLeft = {COUNTDOWN_START};

        updateCountdownLabel(secondsLeft[0]);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    secondsLeft[0]--;
                    updateCountdownLabel(secondsLeft[0]);

                    if (secondsLeft[0] <= 0) {
                        Platform.exit();
                    }
                })
        );
        timeline.setCycleCount(COUNTDOWN_START);
        timeline.play();
    }

    /**
     * Updates the info label with the current countdown value
     * @param secondsLeft seconds remaining before the app closes
     */
    private void updateCountdownLabel(int secondsLeft) {
        infoLabel.setText(
                "Sorry you must be over the age of 18 to use this application\n"
                        + "Closing in " + secondsLeft
        );
    }
}