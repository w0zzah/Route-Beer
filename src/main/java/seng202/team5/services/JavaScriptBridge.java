package seng202.team5.services;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.FastFood;
import seng202.team5.models.locations.Venue;

import java.util.function.Consumer;

/**
 * JavaScriptBridge to connect Leaflet map
 */
public class JavaScriptBridge {

    private static final Logger logger = LogManager.getLogger(JavaScriptBridge.class);

    private final MainManager mainManager;
    private final Consumer<Venue> venueSelectedCallback;
    private Runnable onMapReady;


    public JavaScriptBridge(MainManager mainManager, Consumer<Venue> venueSelectedCallback) {
        this.mainManager = mainManager;
        this.venueSelectedCallback = venueSelectedCallback;
    }

    /** Called from leaflet_map.html when a marker is clicked. */
    public void displayBarInfo(String locID) {
        Bar bar = mainManager.getBars().stream()
                .filter(b -> b instanceof Bar)
                .map(b -> b)
                .filter(b -> b.getLocID().equals(locID))
                .findFirst()
                .orElse(null);

        if (bar == null) {
            logger.warn("No bar found for locID {}", locID);
            return;
        }
        // WebView callbacks already run on the FX thread, but runLater is cheap insurance
        Platform.runLater(() -> venueSelectedCallback.accept(bar));
    }

    public void displayFastFoodInfo(String locID) {
        FastFood fastFood = mainManager.getFastFood().stream()
                .filter(b -> b != null)
                .map(b -> b)
                .filter(b -> b.getLocID().equals(locID))
                .findFirst()
                .orElse(null);

        if (fastFood == null) {
            logger.warn("No Fast Food found for locID {}", locID);
            return;
        }
        // WebView callbacks already run on the FX thread, but runLater is cheap insurance
        Platform.runLater(() -> venueSelectedCallback.accept(fastFood));
    }

    public void setOnMapReady(Runnable r) { this.onMapReady = r; }

    public void onReady() {
        Runnable callback = onMapReady;
        onMapReady = null;
        if (callback != null) {
            Platform.runLater(callback);
        }
    }
}