package seng202.team5.gui.tab_pane_controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import seng202.team5.gui.MapController;
import seng202.team5.gui.ScreenNavigator;
import seng202.team5.gui.util.DialogUtil;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.types.UserType;
import seng202.team5.services.AccountService;
import seng202.team5.services.CommunityRoutesService;
import seng202.team5.services.RouteBuilderService;
import seng202.team5.services.MainManager;
import seng202.team5.services.SavedRoutesService;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Owns only the tab pane, account controls and tab switching
 * Split now into 4 Main parts
 * PART 1: Initialise all important things (self tabs etc.)
 * PART 2: Tab Switching
 * PART 3: Routes
 * PART 4: Bottom Menu + account
 */
public class TabPaneController implements TabPaneCoordinator {

    @FXML
    private Tab routeTab, viewTab, savedTab, communityTab;
    @FXML
    private RouteBuilderTabController routeTabController;
    @FXML
    private RouteDetailsTabController viewTabController;
    @FXML
    private SavedRoutesTabController savedTabController;
    @FXML
    private CommunityRoutesTabController communityTabController;
    @FXML
    private BorderPane tabBorderPane;

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Label titleLabel, accountLabel, usernameLabel;
    @FXML
    private Button tabArrowButton, signOutAccButton, deleteAccButton;
    @FXML
    private SVGPath arrowSVG;
    @FXML
    private ToggleGroup tabButtons;
    @FXML
    private ToggleButton builderTabButton, detailsTabButton, savedTabButton, publicTabButton;
    @FXML
    private VBox menuVBox, accountVBox;

    private static final double MENU_COLLAPSED_WIDTH = 100;
    private static final double MENU_EXPANDED_WIDTH = 200;
    private static final double NAVIGATION_WIDTH = 720;

    private MainManager mainManager;
    private MapController mapController;
    private ScreenNavigator navigator;

    private RouteBuilderService routeBuilderService;
    private SavedRoutesService savedRoutesService;
    private CommunityRoutesService communityRoutesService;
    private AccountService accountService;

    private boolean menuExpanded;

    public TabPaneController() {
    }

    /**
     * Initialises the {@link TabPaneController} by including all the overarching app controllers
     * which are required for it to run
     * Initialises all the service classes as well as their corresponding controllers
     */
    public void initialise(MainManager mainManager, MapController mapController, ScreenNavigator navigator) {
        this.mainManager = mainManager;
        this.mapController = mapController;
        this.navigator = navigator;

        routeBuilderService = new RouteBuilderService(mainManager);
        savedRoutesService = new SavedRoutesService(mainManager);
        communityRoutesService = new CommunityRoutesService(mainManager);
        accountService = new AccountService(mainManager);

        Consumer<Bar> showVenueInfo = bar -> mainManager.venueInfoWidget.show(bar);

        routeTabController.initialise(routeBuilderService, this, showVenueInfo);
        viewTabController.initialise(routeBuilderService, savedRoutesService, this,
                mapController, navigator, showVenueInfo);
        savedTabController.initialise(savedRoutesService, this);
        communityTabController.initialise(communityRoutesService, this);
    }

    /**
     * Initialises the tabs
     */
    public void initialiseTabs() {
        viewTabController.initialiseControls();
        routeTabController.initialiseControls();
        routeTabController.setupParameters();
        initialiseSaveRouteName();
        initialiseSpinner();

        showRouteTab();
        menuExpanded = false;
        toggleMenu();
    }

    /**
     * Initialises spinner
     */
    public void initialiseSpinner() {
        routeTabController.initialiseSpinner();
    }

    /**
     * Initialises the saveRouteName functionality
     */
    public void initialiseSaveRouteName() {
        viewTabController.initialiseSaveRouteName();
    }

    /**
     * Sets the buttons in the app depending on whether the user is signed in or a guest
     * @param user the current user
     */
    public void setAccountButtons(User user) {
        if (Objects.requireNonNull(user.getType()) == UserType.MEMBER) {
            if (!accountVBox.getChildren().contains(deleteAccButton)) {
                accountVBox.getChildren().add(deleteAccButton);
            }
            accountLabel.setText("Username:");
            usernameLabel.setText(user.getUsername());
        } else {
            signOutAccButton.setText("Create an account");
            signOutAccButton.setOnAction(this::returnToCreateAccount);
            accountVBox.getChildren().remove(deleteAccButton);
            accountLabel.setText("You are not signed in");
            usernameLabel.setVisible(false);
            usernameLabel.setManaged(false);
            viewTabController.removeSaveButton();
        }
    }

    /**
     *  PART 2: SWITCH BETWEEN TABS
     */

    /**
     * Shows the called tab
     * @param tab the tab in question
     */
    private void showTab(Tab tab) {
        titleLabel.setText(tab.getText());
        mainTabPane.getSelectionModel().select(tab);
    }

    /**
     * Shows the route tab specifically when called
     */
    @Override
    @FXML
    public void showRouteTab() {
        showTab(routeTab);
        builderTabButton.setSelected(true);
    }

    /**
     * Shows the details tab specifically when called
     */
    @Override
    @FXML
    public void showDetailsTab() {
        showTab(viewTab);
        detailsTabButton.setSelected(true);
    }

    /**
     * Shows the saved routes tab specifically when called
     */
    @Override
    @FXML
    public void showSavedTab() {
        showTab(savedTab);
        savedTabButton.setSelected(true);
    }

    /**
     * Shows the community tab specifically when called
     */
    @Override
    @FXML
    public void showCommunityTab() {
        communityTabController.load();
        showTab(communityTab);
        publicTabButton.setSelected(true);
    }

    /**
     * PART 3: ROUTES
     */

    /**
     * Loads saved routes
     * @param savedRoutes the user's saved routes
     * @param userId the given user's ID
     */
    public void loadSavedRoutes(List<Route> savedRoutes, int userId) {
        savedTabController.load(savedRoutes);
    }

    /**
     * Loads community routes
     * Called the community tab controller to do this
     */
    public void loadCommunityRoutes() {
        communityTabController.load();
    }

    /**
     * Displays the route details  in the route details tab
     * @param route the route being displayed
     */
    public void displayRouteDetails(Route route) {
        viewTabController.showRoute(route);
        showDetailsTab();
    }

    /**
     * Shows the generated route on the map
     * @param route the generated route
     */
    @Override
    public void showGeneratedRoute(Route route) {
        displayRouteDetails(route);
        mapController.displayRoute(route.getBars());
        viewTabController.clearNameAndDescription();
        viewTabController.setRegenButtonVisible(true);
        viewTabController.setRegenButtonVisible(true);

    }

    /**
     * Loads a community or saved route onto the map
     * @param route the route to open
     */
    @Override
    public void openRoute(Route route) {
        viewTabController.clearNameAndDescription();
        displayRouteDetails(route);
        viewTabController.showRouteName(route);
        mapController.displayRoute(route.getBars());
    }

    /**
     * Refreshes saved routes
     */
    @Override
    public void refreshSavedRoutes() {
        savedTabController.load();
    }

    /**
     * Resets the route details and removes the current from teh details
     */
    @Override
    public void resetRouteView() {
        mapController.unloadRoute();
        viewTabController.reset();
    }

    /**
     * Regenerates the route using the same parameters
     */
    @Override
    public void regenerateRoute() {
        routeTabController.generateRoute();
    }

    /**
     * PART 4: MENU + ACCOUNT
     */

    /**
     * Toggle the tab menu
     * Expands and contracts the menu to show or hide tab names
     */
    @FXML
    public void toggleMenu() {
        tabArrowButton.setContentDisplay(menuExpanded ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.RIGHT);
        arrowSVG.setRotate(menuExpanded ? 0 : 180);
        menuVBox.setMaxWidth(menuExpanded ? MENU_COLLAPSED_WIDTH : MENU_EXPANDED_WIDTH);

        tabBorderPane.setPrefWidth(menuExpanded ? NAVIGATION_WIDTH - MENU_COLLAPSED_WIDTH : NAVIGATION_WIDTH - MENU_EXPANDED_WIDTH);

        accountVBox.setVisible(!menuExpanded);
        accountVBox.setManaged(!menuExpanded);

        ContentDisplay contentDisplay = menuExpanded ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT;
        for (Toggle toggle : tabButtons.getToggles()) {
            ((ToggleButton) toggle).setContentDisplay(contentDisplay);
        }

        menuExpanded = !menuExpanded;
    }

    /**
     * Retuns to setup page
     */
    @Override
    @FXML
    public void returnToSetup() {
        returnToSetupPage();
    }

    /**
     * Returns the user to the setup page to create accouny
     * @param event the Create Account button selected
     */
    public void returnToCreateAccount(ActionEvent event) {
        returnToSetupPage();
    }

    /**
     * Checks if the user has a route currently loaded
     * If so, gives an alert to check if they want to sign out and lose any generated route
     * Returns to user to set up
     */
    public void returnToSetupPage() {
        if (routeBuilderService.getCurrentRoute() != null
                && !DialogUtil.confirm("Return To Sign In", "Are you sure you want to exit?",
                "Any generated route will be lost")) {
            return;
        }
        navigator.goToSetup(accountService.isGuest());
    }

    /**
     * Deletes the user account
     * Gives the user an alert for confirmation of account deletion
     */
    @FXML
    public void deleteAccount() {
        if (DialogUtil.confirm("Delete Account", "Are you sure you want to delete this account?",
                "This action cannot be undone")) {
            accountService.deleteAccount();
            navigator.goToSetup(false);
        }
    }
}
