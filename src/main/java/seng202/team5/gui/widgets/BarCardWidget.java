package seng202.team5.gui.widgets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import seng202.team5.models.locations.Bar;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static seng202.team5.App.log;

/**
 * Controller for the Bar cards
 */
public class BarCardWidget {

    @FXML
    BorderPane barBorderPane;
    @FXML
    Label barCardNumber;
    @FXML
    Label barCardName;
    @FXML
    Text barCardAddress;
    @FXML
    ImageView pinImage;
    @FXML
    Button barDeleteButton;

    public static void addRouteBarCard(Bar bar, Pane barsPane, int number, Consumer<? super Bar> onPinClick) {
        BarCardWidget card = loadBarCard();

        if (card != null) {
            if (Objects.equals(barsPane.getId(), "routeDetailsVBox")) {
                card.barBorderPane.setMaxWidth(500);
            }
            card.setRouteBarCardData(bar, number, onPinClick);
            barsPane.getChildren().add(card.barBorderPane);

        }
    }

    /**
     * Adds a particular bar card to the barsVbox for user to see
     * @param bar The bar selected
     * @param barsVBox The Vbox to insert the card into
     * @param onDelete The method to call on delete button clicked
     * @param onPinClick The method to call when the pin is clicked
     */
    public void addParticularBarCard(Bar bar, VBox barsVBox, Consumer<Bar> onDelete, Consumer<? super Bar> onPinClick) {
        BarCardWidget card = loadBarCard();
        if (card != null) {
            card.barBorderPane.setLeft(null);
            card.setBarCardData(bar);
            card.barDeleteButton.setOnAction(e -> {
                barsVBox.getChildren().remove(card.barBorderPane);
                if (onDelete != null) onDelete.accept(bar);
            });
            card.setPinClickHandler(bar, onPinClick);
            barsVBox.getChildren().add(card.barBorderPane);
        }
    }

    /**
     * Loads a card displaying bar information to the screen
     */
    private static BarCardWidget loadBarCard() {
        try {
            FXMLLoader loader = new FXMLLoader(BarCardWidget.class.getResource("/fxml/bar_card.fxml"));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            log.error("Failed to load bar card", e);
            return null;
        }
    }
    /**
     * Updates the bar card data
     * @param bar The bar being represented
     * @param number This bar's position in the route
     */
    public void setRouteBarCardData(Bar bar, int number, Consumer<? super Bar> onPinClick){
        setBarCardData(bar);
        barBorderPane.setRight(null);
        barCardNumber.setText(String.valueOf(number));

        pinImage.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/icons/pin.png"))));
        setPinClickHandler(bar, onPinClick);
    }
    private void setPinClickHandler(Bar bar, Consumer<? super Bar> onPinClick) {
        if (onPinClick == null) {
            return;   // e.g. export window, where there's no venue drawer
        }
        pinImage.setCursor(Cursor.HAND);   // import javafx.scene.Cursor
        pinImage.setOnMouseClicked(e -> onPinClick.accept(bar));
    }
    private void setBarCardData(Bar bar) {
        barCardName.setText(bar.getName());
        barCardAddress.setText(bar.getStrippedAddress());
    }

}
