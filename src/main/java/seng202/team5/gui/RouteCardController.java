package seng202.team5.gui;

import com.sun.tools.javac.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seng202.team5.data.RouteDAO;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.services.MainManager;

import java.io.IOException;

import static seng202.team5.App.log;

/**
 * Class to handle all actions for the route cards. This acts as an MVC controller taking requests from the view,
 * allowing route cards to be added to appropriate containers
 * @author Neela van Gerwen
 */
public class RouteCardController {

    @FXML
    private Label cardNameLabel, routeUsernameLabel, cardBarsLabel, cardDistanceLabel, cardTimeLabel;
    @FXML
    private Text cardDescriptionText;
    @FXML
    private Button loadRouteButton, cardDeleteButton, cardPublishButton;
    @FXML
    private VBox cardButtonsVBox;
    @FXML
    private TextFlow descriptionTextFlow;

    private MapController mapController;
    private Parent cardRoot;
    private VBox parentContainer;
    private Route route;
    private User user;

    public RouteCardController() {}

    /**
     * Creates a card for a given route and loads it into given VBox
     *
     * @param route a Route object to be represented by the card
     * @param routeBox the container for the card to be placed in
     * @param mapController the map controller used to display the route when loaded
     * @param user the current user (reserved for future use, e.g. ownership checks)
     */
    public static void addRouteCard(Route route, VBox routeBox, MapController mapController, TabPaneController tabPaneController, User user, MainManager mainManager) {
        try {
            FXMLLoader loader = new FXMLLoader(RouteCardController.class.getResource("/fxml/route_card.fxml"));
            Parent card = loader.load();
            RouteCardController cardController = loader.getController();

            cardController.mapController = mapController;
            cardController.cardRoot = card;
            cardController.parentContainer = routeBox;
            if (routeBox.getId().equals("communityRouteVBox")) {
                cardController.cardButtonsVBox.getChildren().remove(cardController.cardDeleteButton);
                cardController.cardButtonsVBox.getChildren().remove(cardController.cardPublishButton);
                cardController.routeUsernameLabel.setManaged(true);
                cardController.routeUsernameLabel.setVisible(true);

            } else {
                cardController.routeUsernameLabel.setManaged(false);
                cardController.routeUsernameLabel.setVisible(false);
            }
            cardController.setData(route, tabPaneController, mainManager);

            cardController.route = route;
            cardController.user = user;
            mainManager.currentRoute = route;

            routeBox.getChildren().addFirst(card);
        } catch (IOException e) {
            System.err.println("Failed to load route card: " + e.getMessage());
        }
    }

    /**
     * Update's the route card fields to display individual route information
     *
     * @param route The route this card is representing
     */
    private void setData(Route route, TabPaneController tabPaneController, MainManager mainManager) {
        cardNameLabel.setText(route.getName());
        cardDescriptionText.setText(route.getDescription());
        cardDescriptionText.wrappingWidthProperty().bind(descriptionTextFlow.widthProperty());
        cardBarsLabel.setText(String.valueOf(route.getNumStops()));
        cardDistanceLabel.setText(String.valueOf(route.getTotalDistanceKm()));
        cardTimeLabel.setText(String.valueOf(route.getEstimatedTime()/60));

        loadRouteButton.setOnAction(e -> loadRoute(mapController, tabPaneController, route));
        cardDeleteButton.setOnAction(e -> handleDelete(route, mainManager));
        cardPublishButton.setOnAction(e -> publishRoute(mainManager));
    }

    /**
     * Loads route info
     * @param mapController current MapController
     * @param tabPaneController current TabPaneController
     * @param route current Route
     */
    private void loadRoute(MapController mapController, TabPaneController tabPaneController, Route route) {
        tabPaneController.displayRouteDetails(route);
        tabPaneController.displayRouteName(route);
        mapController.displayRoute(route.getBars());
    }

    /**
     * Deletes the route from the database and removes this card from its container.
     *
     * @param route the route to delete
     */
    private void handleDelete(Route route, MainManager mainManager) {
        try {
            mainManager.routeDAO.deleteSavedRoute(route.getRouteID());
        } catch (Exception e) {
            log.error("Failed to delete route " + route.getRouteID() + ": " + e.getMessage());
            return;
        }

        if (parentContainer != null && cardRoot != null) {
            boolean removed = parentContainer.getChildren().remove(cardRoot);
            if (!removed) {
                log.error("Route card was not found in its container; nothing removed visually");
            }
        }
    }

    /**
     * Publish the route represented by this card
     */
    @FXML
    private void publishRoute(MainManager mainManager) {
        try {
            mainManager.routeDAO.publishCommunityRoute(route, user.getId());
        } catch (Exception e) {
            log.error("Failed to publish route " + route.getRouteID() + ": " + e.getMessage());
        }

    }
}