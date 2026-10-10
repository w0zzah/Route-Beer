package seng202.team5.models.locations;

import seng202.team5.models.Feature;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Map;

/**
 * Bar is a specific location where an end user could get a drink
 * @author Theo Baker
 */

public class Bar extends Venue {

    private ArrayList<Feature> features;
    private Theme theme;
    private String description;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Used as the constructor for OpenStreetMap data
     *
     * @param locID        using OpenStreetMap id here
     * @param name         of location
     * @param latitude     using first coordinate latitude for polygonal points
     * @param longitude    using first coordinate longitude for polygonal points
     * @param description  of the bar
     * @param address      using reverse geocoding of latitude/longitude
     * @param features     using information from bar managers
     * @param openingHours using information from OpenStreetMap, if closing time is 11:59 it indicates venue is open late
     * @param theme        found by hand by developers
     */
    public Bar(String locID, String name, double latitude, double longitude, String description, String address, ArrayList<Feature> features, Map<DayOfWeek, ArrayList<LocalTime>> openingHours, Theme theme) {
        super(
                locID,
                name,
                latitude,
                longitude,
                address,
                openingHours
        );
        this.description = description;
        this.features = features;
        this.theme = theme;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }
    public List<FeatureType> getFeatureTypes() {
        Set<FeatureType> featureTypes = new HashSet<>();
        for (Feature feature : features) {
            featureTypes.add(feature.featureType);
        }
        return new ArrayList<>(featureTypes);
    }

    public String getDescription() {
        return description;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public String toString() {
        return this.getName();
    }



    /**
     * Gets the features of a Bar in enduser readable form
     * @return String of the formatted features
     */
    public String printFeatures() {
        if (this.features.toString().equals("[]")) {
            return null;
        }
        String feaureStr = this.features.toString();
        if (feaureStr.startsWith("[") && feaureStr.endsWith("]")) {
            feaureStr = feaureStr.substring(1, feaureStr.length() - 1);
        }
        return feaureStr;
    }


}
