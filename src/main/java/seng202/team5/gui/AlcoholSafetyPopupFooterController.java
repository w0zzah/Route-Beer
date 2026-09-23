package seng202.team5.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller for the Alcohol safety popup in the footer
 */
public class AlcoholSafetyPopupFooterController {

    @FXML
    private ImageView cheersLogo;

    private MainWindowController mainWindowController;

    /**
     * Initialises the cheers logo to change cursor on hover
     */
    public void initialize() {
        cheersLogo.setOnMouseEntered(event -> cheersLogo.setCursor(Cursor.HAND));
        cheersLogo.setOnMouseExited(event -> cheersLogo.setCursor(Cursor.DEFAULT));
    }

    /**
     * Sets the mainWindowController
     * @param mainWindowController Current instance of the mainWindowController
     */
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    /**
     * Opens Cheers website in browser
     */
    @FXML
    public void openCheersWebsite() {
        Stage stage = (Stage) cheersLogo.getScene().getWindow();
        HostServices hostServices = (HostServices) stage.getProperties().get("hostServices");
        if (hostServices != null) {
            hostServices.showDocument("https://www.cheers.org.nz/");
        }
    }

    /**
     * Close the popup when the 'x' button is pressed
     */
    @FXML
    public void closePopup() {
        mainWindowController.closeAlcoholSafetyPopupFooter();
    }

}
