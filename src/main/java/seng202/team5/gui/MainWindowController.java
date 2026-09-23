package seng202.team5.gui;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import seng202.team5.data.RouteDAO;
import seng202.team5.data.UserDAO;
import seng202.team5.models.*;
import seng202.team5.services.MainManager;
import seng202.team5.services.MemberService;
import seng202.team5.services.UserService;
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
    private Label usernameLabel, accountLabel;
    @FXML
    private Pane accountPane;
    @FXML
    private ToggleButton accountButton;
    @FXML
    private MapController mapController;
    @FXML
    private Button signOutAccButton, deleteAccButton;
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

    private BarInfoController barInfoController;
    private AlcoholSafetyPopupFooterController alcoholSafetyPopupFooterController;
    private ScreenNavigator navigator;
    private MainManager mainManager;
    private TabPaneController tabPaneController;
    private User currentUser;
    private Random random = new Random();
    /**
     * Button action to sign out a user
     */
    @FXML
    public void returnToSetup() {
        navigator.goToSetup(false);
    }

    /**
     * Button action to return to the setup page and toggle create account view
     */
    @FXML
    public void returnToCreateAccount(ActionEvent event) {
        navigator.goToSetup(true);
    }

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
        this.tabPaneController = tabPaneViewController;
        this.currentUser = currentUser;

        // Initialise controllers
        mapController.initialise(mainManager);
        tabPaneController.initialiseTabs();
        tabPaneController.initialise(mainManager, mapController, navigator);
        tabPaneController.setupParameters();
        tabPaneController.initialiseSpinner();

        accountPane.visibleProperty().bind(accountButton.selectedProperty());
        accountPane.managedProperty().bind(accountPane.visibleProperty());

        openAlcoholSafetyPopupAfterDelay(random.nextDouble(0, 30));
        initialiseUser(currentUser);

        // Stop the parked panel (and its drop shadow) painting over the left pane.
        Rectangle mapClip = new Rectangle();
        mapClip.widthProperty().bind(mapPanel.widthProperty());
        mapClip.heightProperty().bind(mapPanel.heightProperty());
        mapPanel.setClip(mapClip);

        barInfoController = BarInfoController.attachTo(barInfoContainer, barInfoSlider, barInfoArrow);
        mapController.setOnBarSelected(bar -> barInfoController.show(bar));
    }

    @FXML
    private void toggleBarInfo() {
        barInfoController.toggle();
    }

    /**
     * Create a new User object and updates GUI accordingly
     * @param currentUser The current user who is logged in, a
     */
    private void initialiseUser(User currentUser) {

        UserService userService;
        // Create a new UserService of the correct type and initialise GUI
        switch (currentUser.getType()) {

            case GUEST -> {
                userService = new UserService(mainManager);
                signOutAccButton.setText("Create an account");
                signOutAccButton.setOnAction(this::returnToCreateAccount);
                accountButtonsHBox.getChildren().remove(deleteAccButton);
                accountLabel.setText("You are not signed in");
                usernameLabel.setVisible(false);
            }
            case ADMIN -> {
                userService = new UserService(mainManager);
                updateAccountPane(currentUser);
            }
            case MEMBER -> {
                // TODO: Rewrite with better integration into DB
                userService = new MemberService(mainManager);

                tabPaneController.loadSavedRoutes((MemberService) userService, currentUser);
                updateAccountPane(currentUser);
            }
            default -> {
                userService = new UserService(mainManager);
            }

        }
        mainManager.setCurrentUser(currentUser, userService);
        tabPaneController.loadCommunityRoutes(userService, currentUser);
    }

    /**
     * Displays the username
     * @param user The current user
     */
    public void updateAccountPane(User user) {
        if (!accountButtonsHBox.getChildren().contains(deleteAccButton)) {
            accountButtonsHBox.getChildren().add(deleteAccButton);
        }
        accountLabel.setText("Username:");
        usernameLabel.setText(user.getUsername());
    }

    /**
     * Deletes the current user's account
     */
    @FXML
    public void deleteAccount() {
        // TODO: Add delay
        mainManager.userDAO.deleteUser(currentUser.getUsername());
        navigator.goToSetup(false);
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
        alcoholSafetyPopupFooterController = loader.getController();
        alcoholSafetyPopupFooterController.setMainWindowController(this);
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