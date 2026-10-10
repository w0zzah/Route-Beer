package seng202.team5.gui;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import seng202.team5.gui.widgets.VenueInfoWidget;
import seng202.team5.gui.tab_pane_controllers.TabPaneController;
import seng202.team5.models.*;
import seng202.team5.models.types.UserType;
import seng202.team5.services.MainManager;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;


import java.io.IOException;
import java.util.Random;

/**
 * FXML controller class for the main window
 * This window manages all the major app functionality, allowing users to view the map and interact with routes
 */
public class MainWindowController {

    @FXML
    private Pane accountPane;
    @FXML
    private MapController mapController;
    @FXML
    private TabPaneController tabPaneViewController;
    @FXML
    private AnchorPane alcoholSafetyPopupFooter, mapPanel;
    @FXML
    private HBox barInfoSlider, accountButtonsHBox;
    @FXML
    private StackPane barInfoContainer;
    @FXML
    private SVGPath barInfoArrow;

    private AlcoholSafetyPopup alcoholSafetyPopup;
    private ScreenNavigator navigator;
    private MainManager mainManager;
    private User currentUser;
    private Random random = new Random();
    private TabPaneController tabPaneController;

    /**
     * Initialises the main window, loading the leaflet map interface and adjusting the GUI considering the
     * current user's UserType
     *
     * @param primaryStage stage to load
     * @param navigator    the screen navigator
     * @param currentUser  the current user
     */
    public void initialise(Stage primaryStage, ScreenNavigator navigator, User currentUser, MainManager mainManager) {
        this.navigator = navigator;
        this.mainManager = mainManager;
        this.currentUser = currentUser;
        this.tabPaneController = tabPaneViewController;

        // Initialise controllers
        mapController.initialise(mainManager);
        tabPaneController.initialise(mainManager, mapController, navigator);
        tabPaneController.initialiseTabs();

        //openAlcoholSafetyPopupAfterDelay(random.nextDouble(0, 30));
        initialiseUser();
        tabPaneController.setAccountButtons(currentUser);

        // Stop the parked panel (and its drop shadow) painting over the left pane.
        Rectangle mapClip = new Rectangle();
        mapClip.widthProperty().bind(mapPanel.widthProperty());
        mapClip.heightProperty().bind(mapPanel.heightProperty());
        mapPanel.setClip(mapClip);

        mainManager.venueInfoWidget = VenueInfoWidget.attachTo(barInfoContainer, barInfoSlider, barInfoArrow);
        mapController.setOnVenueSelected(venue -> mainManager.venueInfoWidget.show(venue));
    }

    @FXML
    private void toggleBarInfo() {
        mainManager.venueInfoWidget.toggle();
    }

    /**
     * Create a new User object and updates GUI accordingly
     */
    private void initialiseUser() {
        mainManager.setCurrentUser(currentUser);

        if (currentUser.getType() == UserType.MEMBER) {
            mainManager.setUserRoutes();
            tabPaneController.loadSavedRoutes(mainManager.getUserRoutes(), currentUser.getId());
        }
        tabPaneController.loadCommunityRoutes();
    }



    /**
     * After a delay, open the alcohol popup footer in the bottom of the map
     * @param delayInSeconds how long to wait before appearing
     */
    private void openAlcoholSafetyPopupAfterDelay(double delayInSeconds) {
        PauseTransition pause = new PauseTransition(Duration.seconds(delayInSeconds));
        pause.setOnFinished(event -> {
            try {
                openAlcoholSafetyPopupFooter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pause.play();
    }

    /**
     * Open a popup formatted in the corner reminding the end user to drink responsibly
     */
    @FXML
    public void openAlcoholSafetyPopupFooter() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/alcohol_safety_popup_footer.fxml"));
        alcoholSafetyPopupFooter = loader.load();
        alcoholSafetyPopup = loader.getController();
        alcoholSafetyPopup.setMainWindowController(this);
        AnchorPane.setBottomAnchor(alcoholSafetyPopupFooter, 10.0);
        AnchorPane.setLeftAnchor(alcoholSafetyPopupFooter, 10.0);
        AnchorPane.setRightAnchor(alcoholSafetyPopupFooter, 10.0);
        mapPanel.getChildren().add(alcoholSafetyPopupFooter);
    }

    /**
     * Close the alcohol safety popup, likely from user input
     */
    public void closeAlcoholSafetyPopupFooter() {
        mapPanel.getChildren().remove(alcoholSafetyPopupFooter);
        openAlcoholSafetyPopupAfterDelay(random.nextDouble(120, 150));
    }

}