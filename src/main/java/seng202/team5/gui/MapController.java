package seng202.team5.gui;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jspecify.annotations.NonNull;
import seng202.team5.models.Bar;
import seng202.team5.models.Location;
import seng202.team5.services.JavaScriptBridge;
import seng202.team5.services.MainManager;
import seng202.team5.services.MapService;
import seng202.team5.services.RouteService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Controller for integration between Leaflet map and GUI
 */
public class MapController {

    @FXML private WebView mapView;
    @FXML private AnchorPane mapPane;

    private MapService mapService;
    private MainManager mainManager;
    private List<Bar> currentRoute;
    private JavaScriptBridge javaScriptBridge;   // must stay a field, or it gets GC'd
    private Consumer<Bar> onBarSelected;
    private AnchorPane amenityTogglePane;

    /**
     * Initialises the map controller
     * @param mainManager the current MainManager
     */
    public void initialise(MainManager mainManager) {
        this.mainManager = mainManager;
        WebEngine engine = mapView.getEngine();
        engine.setJavaScriptEnabled(true);
        this.mapService = new MapService(engine);

        this.javaScriptBridge = new JavaScriptBridge(mainManager, this::handleBarSelected);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaScriptBridge", javaScriptBridge);
                mapService.initialiseMap();
                loadBars();
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Map failed to load: "
                        + engine.getLoadWorker().getException());
            }
        });

        // Kick off the load exactly once, after the listener is registered
        engine.load(Objects.requireNonNull(
                getClass().getResource("/html/leaflet_map.html")).toExternalForm());
    }

    /**
     * Lets MainWindowController receive marker clicks.
     */
    public void setOnBarSelected(Consumer<Bar> handler) {
        this.onBarSelected = handler;
    }

    private void handleBarSelected(Bar bar) {
        if (bar != null && onBarSelected != null) {
            onBarSelected.accept(bar);
        }
    }

    private void loadBars() {
        for (Location location : mainManager.getBars()) {
            mapService.addMarker(location);
        }
    }

    private void displayNearbyFastFood(List<Location> fastFoodLocations) {
        for (Location location : fastFoodLocations) {
            mapService.addFastFoodMarker(location);
        }
    }
    private void displayNearbyPublicToilets(List<Location> publicToiletLocations) {
        for (Location location : publicToiletLocations) {
            mapService.addPublicToiletMarker(location);
        }
    }
    private void displayNearbyDrinkingFountains(List<Location> drinkingFountainLocations) {
        for (Location location : drinkingFountainLocations) {
            mapService.addDrinkingFountainMarker(location);
        }
    }

    public void displayRoute(List<Bar> routeBars) {
        resetMap();
        currentRoute = routeBars;

        mapService.drawRoute(currentRoute);
        displayNearbyFastFood(RouteService.getClosestAmenities(5, mainManager.locationManager.fastFoods, routeBars));
        displayNearbyPublicToilets(RouteService.getClosestAmenities(2, mainManager.locationManager.publicToilets, routeBars));
        displayNearbyDrinkingFountains(RouteService.getClosestAmenities(2, mainManager.locationManager.drinkingFountains, routeBars));
        loadAmenitiesToggle(mapService);
    }

    public void unloadRoute() {
        resetMap();
        loadBars();
    }

    public void resetMap() {
        mapService.clearMap();
        mapService.clearRoute();
    }

    /**
     * Returns a snapshot of the map displaying the route
     * @return the WritableImage snapshot
     */
    public WritableImage takeMapSnapshot() {

        // Gets the bounds of the current route being displayed
        String boundsStr = mapService.getRouteViewBounds();

        // In case an error occurs with the bounds
        if (boundsStr == null || boundsStr.isBlank()) {
            return mapView.snapshot(new SnapshotParameters(), null);
        }
        String[] parts = boundsStr.split(",");

        SnapshotParameters parameters = getSnapshotParameters(parts);

        return mapView.snapshot(parameters, null);
    }

    /**
     * Calculates the parameters for the snapshot
     * @param parts the bounds of the displayed routes
     * @return
     */
    private @NonNull SnapshotParameters getSnapshotParameters(String[] parts) {
        SnapshotParameters parameters = new SnapshotParameters();
        double MIN_HEIGHT = 800;

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double width = Double.parseDouble(parts[2]);
        double height = Double.parseDouble(parts[3]);

        // If bounds height is below MIN_HEIGHT, set to MIN_HEIGHT
        if (height < MIN_HEIGHT) {
            double extraHeight = MIN_HEIGHT - height;
            height = MIN_HEIGHT;
            y -= (extraHeight/2);
        }

        Rectangle2D cropArea = new Rectangle2D(x, y, width, height);
        parameters.setViewport(cropArea);
        return parameters;
    }

    /**
     * Loads the amenities toggle
     * @param mapService the current MapService
     */
    private void loadAmenitiesToggle(MapService mapService) {
        if (amenityTogglePane != null) {
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/toggle_amenities.fxml"));
        loader.setController(new ToggleAmenitiesController(mapService));
        try {
            amenityTogglePane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AnchorPane.setLeftAnchor(amenityTogglePane, 50.0);
        AnchorPane.setTopAnchor(amenityTogglePane, 10.0);
        mapPane.getChildren().add(amenityTogglePane);
    }
}