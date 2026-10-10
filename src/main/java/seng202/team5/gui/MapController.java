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
import seng202.team5.models.Route;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.Location;
import seng202.team5.models.locations.Venue;
import seng202.team5.services.JavaScriptBridge;
import seng202.team5.services.MainManager;
import seng202.team5.services.MapService;
import seng202.team5.services.MapService.AmenityType;
import seng202.team5.services.RouteService;

import java.io.IOException;
import java.io.UncheckedIOException;
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
    private Consumer<Venue> onVenueSelected;
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

        this.javaScriptBridge = new JavaScriptBridge(mainManager, this::handleVenuSelected);

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
    public void setOnVenueSelected(Consumer<Venue> handler) {
        this.onVenueSelected = handler;
    }

    private void handleVenuSelected(Venue venue) {
        if (venue != null && onVenueSelected != null) {
            onVenueSelected.accept(venue);
        }
    }

    private void loadBars() {
        for (Location location : mainManager.getBars()) {
            mapService.addMarker(location);
        }
    }

    /**
     * Adds markers for a single amenity type
     */
    private void displayNearbyAmenities(AmenityType type, List<? extends Location> locations) {
        for (Location location : locations) {
            mapService.addAmenityMarker(type, location);
        }
    }

    /**
     * Displays a route along with the nearest amenities to it
     * @param routeBars the bars making up the route
     */
    public void displayRoute(List<Bar> routeBars) {
        resetMap();
        if (routeBars == null || routeBars.isEmpty()) {
            loadBars();
            return;
        }
        currentRoute = routeBars;

        mapService.drawRoute(currentRoute);
        displayNearbyAmenities(AmenityType.FAST_FOOD,
                RouteService.getClosestAmenities(5, mainManager.locationManager.fastFoods, routeBars));
        displayNearbyAmenities(AmenityType.PUBLIC_TOILET,
                RouteService.getClosestAmenities(2, mainManager.locationManager.publicToilets, routeBars));
        displayNearbyAmenities(AmenityType.DRINKING_FOUNTAIN,
                RouteService.getClosestAmenities(2, mainManager.locationManager.drinkingFountains, routeBars));

        loadAmenitiesToggle();
        amenityTogglePane.setVisible(true);
    }

    /**
     * Removes the route and amenities, and goes back to showing all bars
     */
    public void unloadRoute() {
        resetMap();
        if (amenityTogglePane != null) {
            amenityTogglePane.setVisible(false);
        }
        loadBars();
    }

    public void resetMap() {
        mapService.clearMap();
        mapService.clearRoute();
        currentRoute = null;
    }

    public void exportMap(Consumer<WritableImage> onDone) {
        javaScriptBridge.setOnMapReady(() -> onDone.accept(takeMapSnapshot()));
        mapService.positionForExport();
    }

    /**
     * Returns a snapshot of the map displaying the route
     * @return the WritableImage snapshot
     */
    public WritableImage takeMapSnapshot() {
        String boundsStr = mapService.getRouteViewBounds();

        // In case there is no route or the bounds are unavailable, snapshot the whole view
        if (boundsStr == null || boundsStr.isBlank()) {
            return mapView.snapshot(new SnapshotParameters(), null);
        }
        try {
            SnapshotParameters parameters = getSnapshotParameters(boundsStr.split(","));
            return mapView.snapshot(parameters, null);
        } catch (RuntimeException e) {   // NumberFormatException, ArrayIndexOutOfBounds, bad Rectangle2D
            System.err.println("Invalid snapshot bounds '" + boundsStr + "': " + e.getMessage());
            return mapView.snapshot(new SnapshotParameters(), null);
        }
    }

    /**
     * Calculates the parameters for the snapshot
     * @param parts the bounds of the displayed route as {x, y, width, height}
     * @return the snapshot parameters with the viewport set to the route bounds
     */
    private @NonNull SnapshotParameters getSnapshotParameters(String[] parts) {
        SnapshotParameters parameters = new SnapshotParameters();
        final double minHeight = 800;

        double viewWidth = mapView.getWidth();
        double viewHeight = mapView.getHeight();

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double width = Double.parseDouble(parts[2]);
        double height = Double.parseDouble(parts[3]);

        // Grow the height around the centre if it's too small
        if (height < minHeight) {
            y -= (minHeight - height) / 2;
            height = minHeight;
        }

        width = Math.min(width, viewWidth);
        height = Math.min(height, viewHeight);

        // Slide the rectangle back inside the view
        x = Math.max(0, Math.min(x, viewWidth - width));
        y = Math.max(0, Math.min(y, viewHeight - height));

        parameters.setViewport(new Rectangle2D(x, y, width, height));
        return parameters;
    }

    /**
     * Loads the amenities toggle pane (once). The pane's controller should call
     * mapService.setAmenityVisible(AmenityType, boolean) for each checkbox.
     */
    private void loadAmenitiesToggle() {
        if (amenityTogglePane != null) {
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/toggle_amenities.fxml"));
        loader.setController(new ToggleAmenitiesController(mapService));
        try {
            amenityTogglePane = loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        AnchorPane.setLeftAnchor(amenityTogglePane, 50.0);
        AnchorPane.setTopAnchor(amenityTogglePane, 10.0);
        mapPane.getChildren().add(amenityTogglePane);
    }
}