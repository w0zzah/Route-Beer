package seng202.team5.services;

import seng202.team5.models.Location;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import javafx.scene.web.WebEngine;

/**
 * Provides methods for interacting with js through the webEngine
 * Handles map initialization, markers and displaying routes
 */
public class MapService {
    private final WebEngine webEngine;
    private double[] lats;
    private double[] lngs;

    /**
     * Creates a new MapService using the provided webEngine
     * @param webEngine the WebEngine used to execute js functions
     */
    public MapService(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    /**
     * Initializes the map by calling the js map initialization function
     */
    public void initialiseMap() {
        webEngine.executeScript("jsConnector.initialiseMap()");
    }

    /**
     * Adds a marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addMarker(Location location) {
        String script = String.format("jsConnector.addMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }

    /**
     * Adds a route marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addRouteMarker(Location location) {
        String script = String.format("jsConnector.addRouteMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }
    /**
     * Adds a start marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addStartMarker(Location location) {
        String script = String.format("jsConnector.addStartMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }
    /**
     * Adds a end marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addEndMarker(Location location) {
        String script = String.format("jsConnector.addEndMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }
    /**
     * Adds a fastFood marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addFastFoodMarker(Location location) {
        String script = String.format("jsConnector.addFastFoodMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }
    /**
     * Adds a publicRoute marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addPublicToiletMarker(Location location) {
        String script = String.format("jsConnector.addPublicToiletMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }
    /**
     * Adds a drinkingFountain marker for a given location on the map
     * @param location the location which will be put onto the map as a marker
     */
    public void addDrinkingFountainMarker(Location location) {
        String script = String.format("jsConnector.addDrinkingFountainMarker('%s', '%s', %f, %f)",
                location.getLocID(),
                location.getName().replace("'", "\\'"),
                location.getLatitude(),
                location.getLongitude());
        webEngine.executeScript(script);
    }

    /**
     * Show all amenities on map by showing layer in JS
     */
    public void showAmenities() {
        String script = "jsConnector.showAmenities()";
        webEngine.executeScript(script);
    }

    /**
     * Hide all amenities on map by hiding layer in JS
     */
    public void hideAmenities() {
        String script = "jsConnector.hideAmenities()";
        webEngine.executeScript(script);
    }


    /**
     * Hides all currently displayed markers from the map
     */
    public void clearMap() {
        webEngine.executeScript("jsConnector.clearMap()");
    }

    /**
     * Draw the current route by calling addMarker on each lat, lng of the locations in route
     * @param route the List of Locations in the route to draw on the map
     */
    public void drawRoute(List<? extends Location> route) {
        StringJoiner latLongs = new StringJoiner(", ", "[", "]");
        clearMap();
        Location location = route.getFirst();
        String latlngstr = String.format("L.latLng(%f, %f)", location.getLatitude(), location.getLongitude());
        latLongs.add(latlngstr);
        addStartMarker(location);
        for (int i = 1; i < route.size(); i++) {
            location = route.get(i);
            latlngstr = String.format("L.latLng(%f, %f)", location.getLatitude(), location.getLongitude());
            latLongs.add(latlngstr);
            addRouteMarker(location);
        }
        addEndMarker(location);
        String script = String.format("jsConnector.drawRoute(%s)", latLongs);
        webEngine.executeScript(script);
    }

    /**
     * Removes the currently displayed route from the map
     */
    public void clearRoute() {
        webEngine.executeScript("jsConnector.clearRoute();");
    }

    /**
     * Gets the bounding box around the route for exporting as image
     * @return String of doubles, "x,y,w,h" where x,y represent top left corner of bounding box and w,h represent width and height respectively
     */
    public String getRouteViewBounds() {
        String script = String.format("jsConnector.getSnapshotViewBoundsString(%s, %s)",
                Arrays.toString(this.lats),
                Arrays.toString(this.lngs));
        return (String) webEngine.executeScript(script);
    }
}
