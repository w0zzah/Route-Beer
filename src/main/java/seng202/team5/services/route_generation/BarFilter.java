package seng202.team5.services.route_generation;

import seng202.team5.models.OpenStreetMapLocation;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BarFilter {
    /**
     * Using custom parameters, filters out the bars from all restaurants
     * @param openStreetMapRestaurants Array of OpenStreetMapLocation restaurants to check through
     * @return Array of OpenStreetMap locations that are most likely bars
     */
    public static ArrayList<OpenStreetMapLocation> filter(ArrayList<OpenStreetMapLocation> openStreetMapRestaurants) {

        return openStreetMapRestaurants
                .stream()
                .filter(restaurant ->
                        BarFilter.isOpenLate(restaurant) || BarFilter.hasBarTag(restaurant))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    /**
     * Checks if restaurant has property "bar": "yes"
     * @param restaurant the restaurant being queried
     * @return true if the location has a bartag if not, false
     */
    private static boolean hasBarTag(OpenStreetMapLocation restaurant) {
        if (restaurant.properties.get("bar") != null) {
            return restaurant.properties.get("bar").equals("yes");
        } else {
            return false;
        }
    }

    /**
     * Checks if restaurant has property "opening_hours": <i>open midnight to 4am at least one day</i>
     * @param restaurant the restaurant being queried
     * @return true if the bar is open late
     *         false if the bar has null opening hours or is not open late
     */
    private static boolean isOpenLate(OpenStreetMapLocation restaurant) {
        if (restaurant.properties.get("opening_hours") != null) {
            return ((String) restaurant.properties.get("opening_hours"))
                    .matches(".*-(00:[0-5][0-9]|01:[0-5][0-9]|02:[0-5][0-9]|03:[0-5][0-9]|04:00|24:00).*");
        } return false;
    }
}
