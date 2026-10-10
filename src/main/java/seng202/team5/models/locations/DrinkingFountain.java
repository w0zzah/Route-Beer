package seng202.team5.models.locations;

/**
 * A drinking fountain location for the user to get water, for use in map
 * @author Theo Baker
 */
public class DrinkingFountain extends Location {

    /**
     * Used as the constructor for CCC location data
     *
     * @param locID     using custom id here
     * @param name      of location
     * @param latitude  using first coordinate latitude for polygonal points
     * @param longitude using first coordinate longitude for polygonal points
     */
    public DrinkingFountain(String locID, String name, double latitude, double longitude) {
        super(locID, name, latitude, longitude);
    }
}
