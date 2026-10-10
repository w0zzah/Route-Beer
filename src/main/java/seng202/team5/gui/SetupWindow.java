package seng202.team5.gui;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seng202.team5.services.MainManager;

import java.io.IOException;
import java.util.Objects;

/**
 * Class starts the javaFX application window
 * @author seng202 teaching team
 */
public class SetupWindow extends Application {

    /**
     * Opens the gui with the fxml content specified in resources/fxml/main.fxml
     * @param primaryStage The current fxml stage, handled by javaFX Application class
     * @throws IOException if there is an issue loading fxml file
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        MainManager mainManager = new MainManager();
        ScreenNavigator navigator = new ScreenNavigator(primaryStage, mainManager);

        primaryStage.setTitle("Route Beer");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.getProperties().put("hostServices", getHostServices());
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/brand/logo.png")));
        primaryStage.getIcons().add(icon);

        navigator.goToAgeCheck();
        primaryStage.show();

    }

    /**
     * Launches the FXML application, this must be called from another class (in this cass App.java) otherwise JavaFX
     * errors out and does not run
     * @param args command line arguments
     */
    public static void main(String [] args) {
        launch(args);
    }

}
