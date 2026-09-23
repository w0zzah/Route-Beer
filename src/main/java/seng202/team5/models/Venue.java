package seng202.team5.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * Class for locations that have an address and opening hours
 */
public class Venue extends Location {

    private final String address;
    private final Map<DayOfWeek, ArrayList<LocalTime>> openingHours;

    /**
     * Constructs a new Venue with the specified location details and opening hours
     * @param locID the unique identifier of the venue
     * @param name the name of the venue
     * @param latitude the latitude of the venue
     * @param longitude the longitude of the venue
     * @param address the address of the venue
     * @param openingHours the venue's opening hours
     */
    public Venue(String locID, String name, double latitude, double longitude, String address, Map<DayOfWeek, ArrayList<LocalTime>> openingHours) {
        super(locID, name, latitude, longitude);
        this.address = address;
        this.openingHours = openingHours;
    }

    public String getAddress() {
        return address;
    }

    public Map<DayOfWeek, ArrayList<LocalTime>> getOpeningHours() {
        return openingHours;
    }

    @Override
    public String toString() {
        return super.toString() + "\t" + this.address + "\n";
    }
}
