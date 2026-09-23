package seng202.team5.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seng202.team5.models.Bar;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static seng202.team5.App.log;

/**
 * Controller for the Bar cards
 */
public class BarCardController {

    @FXML
    AnchorPane barCardPane;
    @FXML
    VBox barCardVBox;
    @FXML
    Label barCardNumber, barCardName;
    @FXML
    Text barCardAddress;
    @FXML
    ImageView pinImage;

    /**
     * No arg constructor for controller
     */
    public BarCardController() {}

    /**
     * Loads a card displaying bar information to the screen
     * @param bar The bar to represented on the card
     * @param barsPane The target container for the card to be added to
     * @param number This bar's position in the route
     */
    public void addBarCard(Bar bar, Pane barsPane, int number, int numberOfBars) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bar_card.fxml"));
            Parent barCard = loader.load();
            BarCardController cardController = loader.getController();
            cardController.setBarCardData(bar, number);

            // Uses custom image if the bar is the first or last stop in the route
            String path;
            switch (number) {
                case 1 -> path = "/images/icons/pin-start.png";
                default -> {
                    // Check if the current number is the dynamic maximum value
                    if (number == numberOfBars) {
                        path = "/images/icons/pin-end.png";
                    } else {
                        path = "/images/icons/pin.png";
                    }
                }
            }

            Image image = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream(path)
            ));
            cardController.pinImage.setImage(image);

            // Makes the bar card adjust height for when the address label wraps
            cardController.barCardPane.prefHeightProperty().bind(
                    cardController.barCardVBox.heightProperty().add(5)
            );

            // Sets max width if the bar card is getting added to the export window
            if (Objects.equals(barsPane.getId(), "routeDetailsVBox")) {
                cardController.barCardPane.setMaxWidth(500);
            }

            barsPane.getChildren().add(barCard);

        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    /**
     * Updates the bar card data
     * @param bar The bar being represented
     * @param number This bar's position in the route
     */
    public void setBarCardData(Bar bar, int number) {
        barCardNumber.setText(String.valueOf(number));
        barCardName.setText(bar.getName());
        barCardAddress.setText(bar.getAddress());
    }

}
