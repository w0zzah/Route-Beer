package seng202.team5.services;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.models.Bar;

import java.util.function.Consumer;

/**
 * JavaScriptBridge to connect Leaflet map
 */
public class JavaScriptBridge {

    private static final Logger logger = LogManager.getLogger(JavaScriptBridge.class);

    private final MainManager mainManager;
    private final Consumer<Bar> barSelectedCallback;


    public JavaScriptBridge(MainManager mainManager, Consumer<Bar> barSelectedCallback) {
        this.mainManager = mainManager;
        this.barSelectedCallback = barSelectedCallback;
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
        Platform.runLater(() -> barSelectedCallback.accept(bar));
    }
}