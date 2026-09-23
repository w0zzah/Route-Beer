package seng202.team5.gui;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

/**
 * Controller for the Alcohol safety popup in the TabPane
 */
public class AlcoholSafetyPopupLeftController {

    @FXML
    ImageView cheersLogo, uberLogo;

    /**
     * Opens the Cheers website on click
     */
    @FXML
    public void openCheersWebsite() {
        HostServices hostServices = (HostServices) cheersLogo.getScene().getWindow().getProperties().get("hostServices");
        if (hostServices == null) {
            throw new NullPointerException("HostServices was null");
        }
        hostServices.showDocument("https://www.cheers.org.nz");
    }

    /**
     * Opens the Uber website on click
     */
    @FXML
    public void openUberDownloadWebsite() {
        HostServices hostServices = (HostServices) uberLogo.getScene().getWindow().getProperties().get("hostServices");
        if (hostServices == null) {
            throw new NullPointerException("HostServices was null");
        }
        hostServices.showDocument("https://www.uber.com/nz/en/download/");
    }

}
