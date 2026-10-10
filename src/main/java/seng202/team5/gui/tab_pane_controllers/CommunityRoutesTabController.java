package seng202.team5.gui.tab_pane_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.gui.widgets.RouteCardWidget;
import seng202.team5.services.CommunityRoutesService;

/**
 * Controller for Community Routes Tab. Displays routes shared by users
 * Lets users like or dislike (cheers) each community route
 */
public class CommunityRoutesTabController {

    @FXML
    private VBox communityRouteVBox;
    private CommunityRoutesService communityRoutesService;
    private TabPaneCoordinator coordinator;

    /**
     * Initialises controller with the services it needs
     * @param communityRoutesService helper class for community routes
     * @param coordinator used to open a route in the cards
     */
    public void initialise(CommunityRoutesService communityRoutesService, TabPaneCoordinator coordinator) {
        this.communityRoutesService = communityRoutesService;
        this.coordinator = coordinator;
    }

    /**
     * Loads the community routes and deciphers the user's ability to vote
     * @throws DataAccessException if routes cannot be loaded, and displays that to user
     */
    public void load() {
        communityRouteVBox.getChildren().clear();
        boolean canVote = communityRoutesService.canVote();
        try {
            for (CommunityRoutesService.CommunityRouteEntry entry : communityRoutesService.loadCommunityRoutes()) {
                int routeId = entry.route().getRouteID();
                RouteCardWidget.addCommunityRouteCard(entry.route(), entry.rating(), canVote,
                        communityRouteVBox,
                        coordinator::openRoute,
                        voteValue -> communityRoutesService.vote(routeId, voteValue));
            }
        } catch (DataAccessException e) {
            communityRouteVBox.getChildren().add(new Label("Couldn't load community routes. Try again later."));
        }
    }
}