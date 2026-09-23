package seng202.team5.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import seng202.team5.models.Bar;
import seng202.team5.models.Route;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static seng202.team5.App.log;

/**
 * Controller for the Export window
 * Takes a route and a PNG of the route, displays this with route info
 */
public class ExportWindowController {

    @FXML
    private ImageView exportWindowImage;
    @FXML
    private GridPane exportGridPane;
    @FXML
    private VBox routeDetailsVBox;
    @FXML
    private Label exportNameLabel, exportDescriptionLabel, exportDistanceLabel, exportTimeLabel;

    /**
     * No arg constructor for controller
     */
    public ExportWindowController() {}

    /**
     * Set up export window with route image and details
     * @param mapImage The map image to be loaded on export screen
     * @param route The route object corresponding to this export
     */
    @FXML
    public void initialize(WritableImage mapImage, Route route) {
        exportWindowImage.setImage(mapImage);
        exportDistanceLabel.setText(String.valueOf(route.getTotalDistanceKm()));
        loadBarCards(route);
        exportTimeLabel.setText(String.valueOf(route.getEstimatedTime()/60));
        exportNameLabel.setText(route.getName());
        exportDescriptionLabel.setText(route.getDescription());
    }

    /**
     * Gets the grid pane containing route info to be exported
     * @return grid pane stage
     */
    private Stage getStage() {
        return (Stage) exportGridPane.getScene().getWindow();
    }

    /**
     * Loads the bar cards of the route
     * @param route the route being exported
     */
    public void loadBarCards(Route route) {
        if (route != null) {
            int barNum = 1;
            for (Bar bar : route.getBars()) {
                BarCardController barCardController = new BarCardController();
                barCardController.addBarCard(bar, routeDetailsVBox, barNum, route.getNumStops());
                barNum++;
            }
        }
    }

    /**
     * Takes a snapshot of the grid pane
     * and uses FileChooser to allow the image to be downloaded to a target folder
     */
    @FXML
    public void exportImage() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Save Pane Snapshot");
        fileChooser.setInitialFileName("routebeer_export.png");

        File file = fileChooser.showSaveDialog(getStage());

        // Filter to only allow PNG or JPEG
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files (*.jpg)", "*.jpg")
        );

        if (file != null) {
            try {
                WritableImage writableImage = exportGridPane.snapshot(null, null);
                String format = file.getName().endsWith(".jpg") ? "jpg" : "png";

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), format, file);
                exportComplete();

            } catch (IOException e) {
                log.info(e.getMessage());
            }
        }
    }

    /**
     * Shows an alert and closes window on successful export
     */
    public void exportComplete() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Export complete!");

        alert.showAndWait();
        getStage().close();
    }

    /**
     * Closes window when cancel button pressed
     */
    @FXML
    public void cancelExport() {
        getStage().close();
    }

}


