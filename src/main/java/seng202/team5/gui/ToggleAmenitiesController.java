package seng202.team5.gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import seng202.team5.services.MapService;
import seng202.team5.services.MapService.AmenityType;

/**
 * Controller for the Toggle Amenities
 */
public class ToggleAmenitiesController {

    @FXML private VBox amenitiesKey;
    @FXML private CheckBox amenitiesCheckBox;
    @FXML private CheckBox fastFoodCheckBox;
    @FXML private CheckBox publicToiletCheckBox;
    @FXML private CheckBox drinkingFountainCheckBox;

    private final MapService mapService;

    public ToggleAmenitiesController(MapService mapService) {
        this.mapService = mapService;
    }

    @FXML
    public void initialize() {
        bindAmenityCheckBox(fastFoodCheckBox, AmenityType.FAST_FOOD);
        bindAmenityCheckBox(publicToiletCheckBox, AmenityType.PUBLIC_TOILET);
        bindAmenityCheckBox(drinkingFountainCheckBox, AmenityType.DRINKING_FOUNTAIN);
        amenitiesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                amenitiesCheckBoxTrue();
            } else {
                amenitiesCheckBoxFalse();
            }
        });

        // Sync the map with the initial checkbox states
        if (amenitiesCheckBox.isSelected()) {
            amenitiesCheckBoxTrue();
        } else {
            amenitiesCheckBoxFalse();
        }
    }

    /**
     * Toggles one amenity type on the map when its checkbox changes
     */
    private void bindAmenityCheckBox(CheckBox checkBox, AmenityType type) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (amenitiesCheckBox.isSelected()) {
                mapService.setAmenityVisibility(type, newValue);
            }
        });
    }

    /**
     * Hides every amenity and collapses the list. Individual selections are remembered.
     */
    public void amenitiesCheckBoxFalse() {
        amenitiesKey.setVisible(false);
        amenitiesKey.setManaged(false);
        mapService.hideAllAmenities();
        amenitiesCheckBox.setText("  Show Amenities");
    }

    /**
     * Expands the list and shows only the amenity types that are individually selected
     */
    public void amenitiesCheckBoxTrue() {
        amenitiesKey.setVisible(true);
        amenitiesKey.setManaged(true);
        mapService.showAllAmenities();
        applyIndividualStates();
        amenitiesCheckBox.setText("  Hide Amenities");
    }

    /**
     * Applies each individual checkbox's state to the map
     */
    private void applyIndividualStates() {
        mapService.setAmenityVisibility(AmenityType.FAST_FOOD, fastFoodCheckBox.isSelected());
        mapService.setAmenityVisibility(AmenityType.PUBLIC_TOILET, publicToiletCheckBox.isSelected());
        mapService.setAmenityVisibility(AmenityType.DRINKING_FOUNTAIN, drinkingFountainCheckBox.isSelected());
    }
}