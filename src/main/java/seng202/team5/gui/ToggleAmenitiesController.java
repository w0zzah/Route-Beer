package seng202.team5.gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import seng202.team5.services.MapService;

/**
 * Controller for the Toggle Amenities
 */
public class ToggleAmenitiesController {

    @FXML
    private VBox amenitiesKey;
    @FXML
    private CheckBox amenitiesCheckBox;
    @FXML
    private SVGPath FastFoodSVG, PublicToiletSVG, DrinkingFountainSVG;

    private MapService mapService;

    public ToggleAmenitiesController(MapService mapService) {
        this.mapService = mapService;
    }

    public void initialize() {
        showAmenities();
        amenitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                showAmenities();
            } else {
                hideAmenities();
            }
        });
    }

    public void hideAmenities() {
        amenitiesKey.setVisible(false);
        amenitiesKey.setManaged(false);
        mapService.hideAmenities();
        amenitiesCheckBox.setText("  Show Amenities");

    }

    public void showAmenities() {
        amenitiesKey.setVisible(true);
        amenitiesKey.setManaged(true);
        mapService.showAmenities();
        amenitiesCheckBox.setText("  Hide Amenities");
    }

}
