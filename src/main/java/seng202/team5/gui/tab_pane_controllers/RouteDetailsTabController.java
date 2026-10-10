package seng202.team5.gui.tab_pane_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seng202.team5.gui.MapController;
import seng202.team5.gui.ScreenNavigator;
import seng202.team5.gui.util.DialogUtil;
import seng202.team5.gui.util.FieldErrorUtil;
import seng202.team5.gui.widgets.BarCardWidget;
import seng202.team5.models.Route;
import seng202.team5.models.locations.Bar;
import seng202.team5.services.RouteBuilderService;
import seng202.team5.services.SavedRoutesService;

import java.util.function.Consumer;

/**
 * Controller for the route details tab: shows the current route's bars,
 * and lets the user name, save, export or clear it.
 */
public class RouteDetailsTabController {

    @FXML
    private VBox routeDetailsVBox;
    @FXML
    private VBox routeDetailsTabVBox;
    @FXML
    private VBox routeBarsVBox;
    @FXML
    private GridPane noRouteGrid;
    @FXML
    private Label walkingDistanceLabel;
    @FXML
    private Label estTimeLabel;
    @FXML
    private Label routeNameErrorLabel;
    @FXML
    private TextField routeNameField;
    @FXML
    private TextArea routeDescriptionField;
    @FXML
    private HBox routeNameHBox;
    @FXML
    private Button saveRouteButton;
    @FXML
    private Button regenerateRouteButton;

    private RouteBuilderService routeBuilderService;
    private SavedRoutesService savedRoutesService;
    private TabPaneCoordinator coordinator;
    private MapController mapController;
    private ScreenNavigator navigator;
    private Consumer<Bar> showVenueInfo;

    private static final int MAX_DESCRIPTION = 65;
    private static final int MAX_ROUTE_NAME = 30;

    /**
     * @param routeBuilderService current-route access
     * @param savedRoutesService   saving and name checks
     * @param coordinator         the control panel
     * @param mapController       map used for export
     * @param navigator           screen navigator used for export window
     * @param showVenueInfo       opens the venue info popup for a bar
     */
    public void initialise(RouteBuilderService routeBuilderService, SavedRoutesService savedRoutesService,
                           TabPaneCoordinator coordinator, MapController mapController,
                           ScreenNavigator navigator, Consumer<Bar> showVenueInfo) {
        this.routeBuilderService = routeBuilderService;
        this.savedRoutesService = savedRoutesService;
        this.coordinator = coordinator;
        this.mapController = mapController;
        this.navigator = navigator;
        this.showVenueInfo = showVenueInfo;
    }

    /**
     * View-only setup: save button binding and the empty "no route" state.
     */
    public void initialiseControls() {
        disableSaveButtonWhenNameEmpty();
        showEmpty();
        initialiseRouteDescription();

    }

    /**
     * Initialises formatting for the route description field to put a limit on character entry
     */
    private void initialiseRouteDescription() {
        routeDescriptionField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() > MAX_DESCRIPTION) {
                FieldErrorUtil.show(routeDescriptionField, routeNameErrorLabel, String.format("Description cannot exceed %d characters", MAX_DESCRIPTION));
                return null;
            }
            FieldErrorUtil.clear(routeDescriptionField, routeNameErrorLabel);
            return change;
        }));
    }

    /**
     * Listens to the route name field and flags names the user has already used.
     */
    public void initialiseSaveRouteName() {
        // Limits the amount of characters for route name
        routeNameField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > MAX_ROUTE_NAME) {
                FieldErrorUtil.show(routeNameField, routeNameErrorLabel,
                        String.format("Route name cannot exceed %d characters", MAX_ROUTE_NAME));
                return null;
            }
            return change;
        }));

        // Makes sure the user doesn't already have a rout with this name
        routeNameField.textProperty().addListener((observable, oldValue, newName) -> {
            if (savedRoutesService.isNameTaken(newName)) {
                FieldErrorUtil.show(routeNameField, routeNameErrorLabel, "Route name already exists");
            } else {
                clearRouteNameError();
            }
        });
    }

    /**
     * Makes the route the current route and shows its bars.
     *
     * @param route route to display
     */
    public void showRoute(Route route) {
        routeDetailsVBox.setVisible(true);
        routeDetailsVBox.setManaged(true);
        routeBuilderService.setCurrentRoute(route);
        routeDetailsTabVBox.getChildren().remove(noRouteGrid);
        routeBarsVBox.getChildren().clear();
        loadBarCards(route, routeBarsVBox);
    }

    /**
     * Fills the name and description fields from a stored route.
     *
     * @param route route whose name and description to show
     */
    public void showRouteName(Route route) {
        routeNameField.setText(route.getName());
        routeDescriptionField.setText(route.getDescription());
        clearRouteNameError();
    }

    /**
     * Removes the save button from details page
     */
    public void removeSaveButton() {
        routeNameHBox.getChildren().remove(saveRouteButton);
    }

    /**
     * Adds bar cards to the given container and updates distance/time labels.
     *
     * @param route route being represented
     * @param destination where the cards go
     */
    public void loadBarCards(Route route, VBox destination) {
        if (route == null) {
            return;
        }
        walkingDistanceLabel.setText(String.format("%.1f", route.getTotalDistanceKm()));
        estTimeLabel.setText(route.getFormattedTime());
        int barNum = 1;
        for (Bar bar : route.getBars()) {
            BarCardWidget.addRouteBarCard(bar, destination, barNum, b -> showVenueInfo.accept(b));
            barNum++;
        }
    }

    /**
     * Shows the "no route yet" state.
     */
    public void showEmpty() {
        routeDetailsVBox.setVisible(false);
        if (!routeDetailsTabVBox.getChildren().contains(noRouteGrid)) {
            routeDetailsTabVBox.getChildren().add(noRouteGrid);
        }
        setRegenButtonVisible(false);
    }

    /**
     * Clears the displayed route, name fields, and the current route itself.
     */
    public void reset() {
        routeBarsVBox.getChildren().clear();
        clearNameAndDescription();
        showEmpty();
        routeBuilderService.setCurrentRoute(null);
    }

    /**
     * The regenerate button stays in the FXML layout; it is only toggled.
     *
     * @param visible whether to show it
     */
    public void setRegenButtonVisible(boolean visible) {
        regenerateRouteButton.setVisible(visible);
        regenerateRouteButton.setManaged(visible);
    }

    /**
     * Clears the name and description fields of the save route
     */
    @FXML
    public void clearNameAndDescription() {
        routeNameField.clear();
        routeDescriptionField.clear();
    }

    /**
     * Saves the current route under the entered name and description.
     * Results in an alert for the user if something goes wrong
     */
    @FXML
    public void callSaveRoute() {
        Route route = routeBuilderService.getCurrentRoute();
        if (route == null) {
            return;
        }
        SavedRoutesService.SaveResult result = savedRoutesService.saveRoute(
                route, routeNameField.getText(), routeDescriptionField.getText());

        switch (result) {
            case SAVED -> {
                coordinator.refreshSavedRoutes();
                DialogUtil.info("Save Route", "Route Successfully Saved!");
                coordinator.showSavedTab();
            }
            case NAME_TAKEN -> FieldErrorUtil.show(routeNameField, routeNameErrorLabel,
                    "Route name already exists");
            case FAILED -> DialogUtil.info("Save Route", "Route Unable To Be Saved");
        }
    }

    /**
     * Calls the export functionality by displaying a preview
     * of the route to the user
     */
    @FXML
    public void onExport() {
        Route currentRoute = routeBuilderService.getCurrentRoute();
        mapController.exportMap(mapSnapshot -> navigator.launchExportWindow(mapSnapshot, currentRoute));
    }

    /**
     * Clears the map of its current route
     */
    @FXML
    public void clearRoute() {
        if (DialogUtil.confirm("Clear Route", "Are you sure you want to clear this route?",
                "This action cannot be undone")) {
            setRegenButtonVisible(false);
            coordinator.resetRouteView();
            coordinator.showRouteTab();
        }
    }

    /**
     * Handler for the regenerate button.
     */
    @FXML
    public void regenerate() {
        coordinator.regenerateRoute();
    }

    /**
     * Handler for the "Browse Routes" button in the empty state.
     */
    @FXML
    public void showCommunityTab() {
        coordinator.showCommunityTab();
    }

    /**
     * Handler for the "Make a Route" button in the empty state.
     */
    @FXML
    public void showRouteTab() {
        coordinator.showRouteTab();
    }

    /**
     * Clears the error formatting from the Route Name field
     */
    private void clearRouteNameError() {
        FieldErrorUtil.clear(routeNameField, routeNameErrorLabel);
    }

    /**
     * Disables the save button when there is no name the in the field
     */
    private void disableSaveButtonWhenNameEmpty() {
        Tooltip tooltip = new Tooltip("Please enter a route name");
        tooltip.setShowDelay(Duration.millis(100));

        saveRouteButton.disableProperty().bind(routeNameField.textProperty().isEmpty());
        saveRouteButton.disabledProperty().addListener((obs, wasDisabled, isDisabled) -> {
            if (Boolean.TRUE.equals(isDisabled)) {
                Tooltip.install(routeNameHBox, tooltip);
            } else {
                Tooltip.uninstall(routeNameHBox, tooltip);
            }
        });

        if (saveRouteButton.isDisabled()) {
            Tooltip.install(routeNameHBox, tooltip);
        }
    }
}
