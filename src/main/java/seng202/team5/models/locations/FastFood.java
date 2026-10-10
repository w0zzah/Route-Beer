package seng202.team5.models.locations;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * FastFood holds locations that a user to quickly get food on a pub crawl
 * @author Theo Baker
 */
public class FastFood extends Venue {

    /**
     * Used as the constructor for OpenStreeMap data
     *
     * @param locID     using OpenStreetMap id here
     * @param name      of location
     * @param address using reverse geocoding
     * @param latitude  using first coordinate latitude for polygonal points
     * @param longitude using first coordinate longitude for polygonal points
     * @param openingHours using information from online
     */
    public FastFood(String locID, String name, String address, double latitude, double longitude, Map<DayOfWeek, ArrayList<LocalTime>> openingHours) {
        super(locID, name, latitude, longitude, address, openingHours);
    }
}
