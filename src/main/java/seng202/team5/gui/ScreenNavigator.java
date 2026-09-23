package seng202.team5.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.services.MainManager;

import java.io.IOException;

/**
 * The type Screen navigator.
 */
public class ScreenNavigator {

    private final Stage stage;
    private final Scene scene;
    private final MainManager mainManager;
    private User user;

    /**
     * Instantiates a new Screen navigator.
     * @param stage       the stage
     */
    public ScreenNavigator(Stage stage, MainManager mainManager) {
        this.stage = stage;
        this.scene = new Scene(new Pane(), 1280, 720);
        this.mainManager = mainManager;
        this.stage.setScene(this.scene);
        this.stage.setTitle("Route Finder");
    }

    /**
     * Initialise
     */
    public void goToAgeCheck() {
        loadScreen("/fxml/age_check.fxml", controller -> {
            if (controller instanceof AgeCheckController ageCheckController) {
                ageCheckController.initialise(stage, this);
            }
        });
    }
    /**
     * Go to setup page
     */
    public void goToSetup(boolean accountPage) {
        loadScreen("/fxml/setup.fxml", controller -> {
            if (controller instanceof EntryController entryController) {
                entryController.initialise(stage, this, accountPage);
            }
        });
    }

    /**
     * Go to the MainWindow
     */
    public void goToMain(User currentUser) {
        loadScreen("/fxml/main_window.fxml", controller -> {
            if (controller instanceof MainWindowController mainWindowController) {
                mainWindowController.initialise(stage, this, currentUser, mainManager);
            }
        });
    }
    /**
     * Go to the data page
     */
    public void goToData() {
        loadScreen("/fxml/data_view.fxml", controller -> {
            if (controller instanceof DataViewController dataViewController) {
                dataViewController.initialise(this, mainManager);
            }
        });
    }

    /**
     * Loads window for given FXML path
     * @param fxmlPath FXML file to load
     * @param injector controller injector
     */
    private void loadScreen(String fxmlPath, ControllerInjector injector) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            injector.inject(controller);

            scene.setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML file at location: " + fxmlPath, e);
        }
    }

    /**
     * Launches the export window
     * @param mapSnapshot route image to be displayed
     * @param route the route being exported
     */
    public void launchExportWindow(WritableImage mapSnapshot, Route route) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/export.fxml"));
            Parent root = loader.load();

            ExportWindowController exportWindowController = loader.getController();
            exportWindowController.initialize(mapSnapshot, route);

            Stage newStage = new Stage();
            newStage.setTitle("Export Route");

            newStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load export FXML file. Check the path configuration.");
        }
    }


    @FunctionalInterface
    private interface ControllerInjector {
        /**
         * Inject.
         *
         * @param controller the controller
         */
        void inject(Object controller) throws IOException;
    }
}