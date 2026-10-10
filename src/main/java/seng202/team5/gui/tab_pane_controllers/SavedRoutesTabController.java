package seng202.team5.gui.tab_pane_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import seng202.team5.gui.util.DialogUtil;
import seng202.team5.gui.widgets.RouteCardWidget;
import seng202.team5.models.Route;
import seng202.team5.services.SavedRoutesService;

import java.util.List;

/**
 * Controller for the saved routes tab.
 */
public class SavedRoutesTabController {

    @FXML
    private VBox savedRouteVBox;
    @FXML
    private GridPane savedGuestGrid;
    @FXML
    private Button signInSavedButton;
    @FXML
    private Label savedTabLabel;

    private SavedRoutesService savedRoutesService;
    private TabPaneCoordinator coordinator;

    /**
     * @param savedRoutesService saved route logic
     * @param coordinator the control panel
     */
    public void initialise(SavedRoutesService savedRoutesService, TabPaneCoordinator coordinator) {
        this.savedRoutesService = savedRoutesService;
        this.coordinator = coordinator;
    }

    /**
     * Reloads the tab from the current user's saved routes.
     */
    public void load() {
        load(savedRoutesService.getSavedRoutes());
    }

    /**
     * Shows the given routes.
     *
     * @param savedRoutes routes to display
     */
    public void load(List<Route> savedRoutes) {
        savedRouteVBox.getChildren().clear();
        if (savedRoutes.isEmpty()) {
            noSavedRoutes();
        }
        for (Route route : savedRoutes) {
            RouteCardWidget.addSavedRouteCard(
                    route,
                    savedRouteVBox,
                    coordinator::openRoute,
                    this::confirmAndDelete,
                    savedRoutesService::publishRoute);
        }
    }

    /**
     * Adds a grid to the saved route VBox for when there are no saved routes
     */
    private void noSavedRoutes() {
        savedRouteVBox.getChildren().add(savedGuestGrid);
        signInSavedButton.setText("New Route");
        signInSavedButton.setOnAction(e -> coordinator.showRouteTab());
        savedTabLabel.setText("You have no saved routes");
    }

    /**
     * Handler for the guest "Sign in" button.
     */
    @FXML
    public void returnToSetup() {
        coordinator.returnToSetup();
    }

    /**
     * Prompts the user before they delete a route
     * @param route The route being deleted
     */
    private void confirmAndDelete(Route route) {
        if (DialogUtil.confirm("Delete Saved Route", "Are you sure you want to delete this route?",
                "This action cannot be undone")) {
            savedRoutesService.deleteRoute(route);
            load();
            coordinator.resetRouteView();
        }
    }
}
