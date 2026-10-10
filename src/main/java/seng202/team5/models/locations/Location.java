package seng202.team5.models.locations;

/**
 * Location holds info about a visitable location in route
 * @author Theo Baker
 */

public class Location {

    private final String locID;
    private final String name;
    private final double latitude;
    private final double longitude;

    /**
     * Used as the constructor for OpenStreeMap data
     * @param locID using openStreetMap id here
     * @param name of location
     * @param latitude using first coordinate latitude for polygonal points
     * @param longitude using first coordinate longitude for polygonal points
     */
    public Location(String locID, String name, double latitude, double longitude) {
        this.locID = locID;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocID() {
        return locID;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Formats location for call to Geoapify API
     * @return String of correct Geoapify format
     */
    public String getLocationForGeoapify() {
        return String.format("{\"location\": [%.7f, %.7f]}", longitude, latitude);
    }

    @Override
    public String toString() {
        return this.getName() + " (" + this.getLatitude() + ", " + this.getLongitude() + ")\n";
    }

}
