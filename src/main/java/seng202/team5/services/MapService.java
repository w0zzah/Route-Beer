package seng202.team5.services;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import seng202.team5.models.locations.Location;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Provides methods for interacting with js through the webEngine
 * Handles map initialization, markers and displaying routes
 */
public class MapService {

    /**
     * The amenity types that can be shown/hidden individually.
     * The key must match the keys used in leaflet_map.html.
     */
    public enum AmenityType {
        FAST_FOOD("fastFood"),
        PUBLIC_TOILET("publicToilet"),
        DRINKING_FOUNTAIN("drinkingFountain");

        private final String jsKey;

        AmenityType(String jsKey) {
            this.jsKey = jsKey;
        }

        public String getJsKey() {
            return jsKey;
        }
    }

    private final WebEngine webEngine;

    /**
     * Creates a new MapService using the provided webEngine
     * @param webEngine the WebEngine used to execute js functions
     */
    public MapService(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    /**
     * Calls a function on the jsConnector where func and args are sent individually each time
     * @param function The JavaScript function name inside leaflet.html
     * @param args The object to be passed into the JavaScript function
     */
    private Object callJsConnector(String function, Object... args) {
        JSObject connector = (JSObject) webEngine.executeScript("jsConnector");
        return connector.call(function, args);
    }

    /**
     * Initializes the map inside of leaflet_map.html
     */
    public void initialiseMap() {
        callJsConnector("initialiseMap");
    }

    /**
     * base add marker function that all markers take
     * @param function "add(x)Marker" function, for base and each function called below
     * @param location the location of the marker to add
     */
    private void callMarkerFunction(String function, Location location) {
        callJsConnector(function,
                String.valueOf(location.getLocID()),
                Objects.toString(location.getName(), ""),
                location.getLatitude(),
                location.getLongitude());
    }

    private void callMarkerFunction(String function, Location location, int number) {
        callJsConnector(function,
                String.valueOf(location.getLocID()),
                Objects.toString(location.getName(), ""),
                location.getLatitude(),
                location.getLongitude(),
                number);
    }

    public void addMarker(Location location) {
        callMarkerFunction("addMarker", location);
    }

    public void addRouteMarker(Location location, int number) {
        callMarkerFunction("addRouteMarker", location, number);
    }
    /**
     * Adds amenity marker to the map with type and location.
     */
    public void addAmenityMarker(AmenityType type, Location location) {
        callJsConnector("addAmenityMarker",
                type.getJsKey(),
                String.valueOf(location.getLocID()),
                Objects.toString(location.getName(), ""),
                location.getLatitude(),
                location.getLongitude());
    }
    public void addFastFoodMarker(Location location) {
        addAmenityMarker(AmenityType.FAST_FOOD, location);
    }
    public void addPublicToiletMarker(Location location) {
        addAmenityMarker(AmenityType.PUBLIC_TOILET, location);
    }
    public void addDrinkingFountainMarker(Location location) {
        addAmenityMarker(AmenityType.DRINKING_FOUNTAIN, location);
    }

    /**
     * Shows or hides a single amenity type on the map
     * @param type the amenity type to toggle
     * @param visible true to show, false to hide
     */
    public void setAmenityVisibility(AmenityType type, boolean visible) {
        callJsConnector("setAmenityVisibility", type.getJsKey(), visible);
    }

    public void showAllAmenities() {
        for (AmenityType type : AmenityType.values()) {
            setAmenityVisibility(type, true);
        }
    }

    /**
     * Hide all amenities on the map
     */
    public void hideAllAmenities() {
        for (AmenityType type : AmenityType.values()) {
            setAmenityVisibility(type, false);
        }
    }

    /**
     * Removes all currently displayed markers from the map
     */
    public void clearMap() {
        callJsConnector("clearMap");
    }

    /**
     * Draws the route and places start, stop and end markers
     * @param route the List of Locations in the route to draw on the map
     */
    public void drawRoute(List<? extends Location> route) {
        if (route == null || route.isEmpty()) {
            return;
        }
        clearMap();

        StringJoiner waypoints = new StringJoiner(",", "[", "]");
        for (Location location : route) {
            waypoints.add(String.format(Locale.ROOT, "[%f,%f]",
                    location.getLatitude(), location.getLongitude()));
        }

        for (int i = 0; i < route.size(); i++) {
            addRouteMarker(route.get(i), i+1);
        }
        callJsConnector("drawRoute", waypoints.toString());
    }

    public void clearRoute() {
        callJsConnector("clearRoute");
    }

    /**
     * Gets the bounding box around the route for exporting as image
     * @return String of doubles, "x,y,w,h" where x,y represent top left corner of bounding box and w,h represent width and height respectively
     */
    public String getRouteViewBounds() {
        Object result = callJsConnector("getSnapshotViewBoundsString");
        return result instanceof String s ? s : "";
    }

    public void positionForExport() {
        String script = "jsConnector.positionForExport()";
        webEngine.executeScript(script);
    }



}