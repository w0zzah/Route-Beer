package seng202.team5.models.locations;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Class for locations that have an address and opening hours
 */
public class Venue extends Location {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");
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

    public String getStrippedAddress() {
        return address.split(",", 2)[0];
    }

    public Map<DayOfWeek, ArrayList<LocalTime>> getOpeningHours() {
        return openingHours;
    }

    @Override
    public String toString() {
        return super.toString() + "\t" + this.address + "\n";
    }

    /**
     * Gets the hours of this Bar in a printable, end user readable form
     * @return String of the formatted bar hours
     */
    public String printHours() {
        Map<DayOfWeek, ArrayList<LocalTime>> openingHours = this.getOpeningHours();
        StringBuilder hours = new StringBuilder();
        if (openingHours == null || openingHours.isEmpty()) {
            return ("Hours unavailable");
        }
        String dayHours;
        for (DayOfWeek day : DayOfWeek.values()) {
            ArrayList<LocalTime> times = openingHours.get(day);
            dayHours = String.format("%s, %s\n",
                    day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    formatHours(times));
            hours.append(dayHours);
        }
        return hours.toString();
    }

    /**
     * Converts list of times into printable, end user readable form
     * @param times ArrayList of times
     * @return String of the formatted hours
     */
    private String formatHours(ArrayList<LocalTime> times) {
        if (times == null || times.size() < 2 || times.get(0) == null || times.get(1) == null) {
            return "Closed";
        }
        LocalTime close = times.get(1);
        String closeText = close.equals(LocalTime.of(23, 59)) ? "Late" : TIME_FMT.format(close);
        return TIME_FMT.format(times.get(0)) + " - " + closeText;
    }
}
