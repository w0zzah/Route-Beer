package seng202.team5.gui.widgets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import seng202.team5.models.Route;
import seng202.team5.models.RouteRating;

import java.io.IOException;
import java.util.function.Consumer;

import static seng202.team5.App.log;

/**
 * Class to handle all actions for the route cards. This acts as an MVC controller taking requests from the view,
 * allowing route cards to be added to appropriate containers
 * @author Neela van Gerwen
 */
public class RouteCardWidget {

    @FXML
    private StackPane routeCardPane;
    @FXML
    private Label routeUsernameLabel;
    @FXML
    private Label cardBarsLabel;
    @FXML
    private Label cardDistanceLabel;
    @FXML
    private Label cardTimeLabel;
    @FXML
    private Label cardNameLabel;
    @FXML
    private Label upvoteLabel;
    @FXML
    private Label downvoteLabel;
    @FXML
    private Label cardDescriptionLabel;
    @FXML
    private Button loadRouteButton;
    @FXML
    private Button cardDeleteButton;
    @FXML
    private Button cardPublishButton;
    @FXML
    private HBox usernameHBox;
    @FXML
    private HBox cardBaseHBox;
    @FXML
    private HBox cardTopHBox;
    @FXML
    private GridPane routeVoteGrid;
    @FXML
    private ToggleButton upvoteButton;
    @FXML
    private ToggleButton downvoteButton;
    @FXML
    private ToggleGroup voteGroup;
    @FXML
    private Separator cardBaseSeparator;


    private Consumer<Route> onLoad;
    private RouteRating routeRating;

    /**
     * Creates a new blank route card
     * @return The new RouteCardWidget
     */
    private static RouteCardWidget loadRouteCard() {
        try {
            FXMLLoader loader = new FXMLLoader(RouteCardWidget.class.getResource("/fxml/route_card.fxml"));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            log.error("Failed to load bar card", e);
            return null;
        }
    }

    /**
     * Adds a route card to be displayed on the Saved Routes tab
     * @param route the route this card is representing
     * @param routeBox the target container for the card
     * @param onLoad consumer to load the route to the map on load button pressed
     * @param onDelete consumer to delete route from saved routes on delete button pressed
     * @param onPublish consumer to publish the route on publish button pressed
     */
    public static void addSavedRouteCard(Route route, VBox routeBox, Consumer<Route> onLoad, Consumer<Route> onDelete, Consumer<Route> onPublish) {

        RouteCardWidget card = loadRouteCard();

        if (card != null) {
            card.onLoad = onLoad;
            card.cardDeleteButton.setOnAction(e -> { if (onDelete != null) onDelete.accept(route); });
            card.cardPublishButton.setOnAction(e -> { if (onPublish != null) onPublish.accept(route); });
            card.setSavedRouteData(route);
            routeBox.getChildren().addFirst(card.routeCardPane);
        }
    }

    /**
     * Sets data and removes buttons unnecessary for saved routes
     * @param route the route this card is representing
     */
    private void setSavedRouteData(Route route) {
        setData(route);
        cardBaseHBox.getChildren().remove(usernameHBox);
        cardBaseHBox.getChildren().remove(cardBaseSeparator);
        cardTopHBox.getChildren().remove(routeVoteGrid);
    }

    /**
     * Adds a route card to be displayed on the Public Routes tab
     * @param route the route being represented by this card
     * @param routeRating an object holding the upvotes, downvotes and user's vote for this route
     * @param canVote boolean to disable vote buttons if user is a guest
     * @param routeBox the target container for the card
     * @param onLoad consumer to load the route to the map on load button pressed
     * @param onVote consumer to allow user to vote on this route
     */
    public static void addCommunityRouteCard(Route route, RouteRating routeRating, boolean canVote, VBox routeBox, Consumer<Route> onLoad, Consumer<Integer> onVote) {
        RouteCardWidget card = loadRouteCard();

        if (card != null) {

            card.onLoad = onLoad;
            card.routeRating = routeRating;
            card.setCommunityRouteData(route);

            if (canVote) {
                // Updates card vote when toggle group is interacted with
                card.voteGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                    // Sets vote value to 1 if newToggle is up vote, -1 if down vote, and 0 if nothing selected
                    int value = newToggle == card.upvoteButton ? 1 : newToggle == card.downvoteButton ? -1 : 0;
                    card.routeRating = routeRating.voteChange(value);
                    card.updateCardVote(card.routeRating);
                    if (onVote != null) onVote.accept(value);
                });
            } else {
                // Disables vote buttons and adds tooltip for guest users
                card.upvoteButton.setDisable(true);
                card.downvoteButton.setDisable(true);
                card.routeVoteGrid.setPickOnBounds(true);
                Tooltip signInTooltip = new Tooltip("You must be signed in to leave a vote.");
                signInTooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(card.routeVoteGrid, signInTooltip);
            }

            routeBox.getChildren().addFirst(card.routeCardPane);
        }
    }

    /**
     * Sets data for community routes and removes unnecessary buttons
     * @param route the route being represented by this card
     */
    private void setCommunityRouteData(Route route) {
        setData(route);
        cardBaseHBox.getChildren().remove(cardDeleteButton);
        cardBaseHBox.getChildren().remove(cardPublishButton);
        loadRouteButton.setMaxWidth(120);

        routeUsernameLabel.setText(route.getOwnerUsername());
        downvoteLabel.setText(String.valueOf(routeRating.dislikes()));
        upvoteLabel.setText(String.valueOf(routeRating.likes()));

        // Selects corresponding toggle button if user had previously left a rating
        int userVote = routeRating.userVote();
        upvoteButton.setSelected(userVote == 1);
        downvoteButton.setSelected(userVote == -1);
    }

    /**
     * Updates the vote count on the card
     * @param routeRating the RouteRating object holding this route's vote data
     */
    public void updateCardVote(RouteRating routeRating) {
        upvoteLabel.setText(String.valueOf(routeRating.likes()));
        downvoteLabel.setText(String.valueOf(routeRating.dislikes()));
    }

    /**
     * Update the base fields for this route card
     * @param route The route this card is representing
     */
    private void setData(Route route) {
        cardDescriptionLabel.setText(route.getDescription());
        cardBarsLabel.setText(String.valueOf(route.getNumStops()));
        cardDistanceLabel.setText(String.format("%.1f", route.getTotalDistanceKm()));
        cardTimeLabel.setText(route.getFormattedTime());
        cardNameLabel.setText(route.getName());
        loadRouteButton.setOnAction(e -> { if (onLoad != null) onLoad.accept(route); });
    }
}