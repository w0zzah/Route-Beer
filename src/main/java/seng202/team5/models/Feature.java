package seng202.team5.models;

import seng202.team5.models.types.FeatureType;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Holds times and featuretype for feature info of a particular bar
 * @author Theo Baker
 */

public class Feature {
    public final FeatureType featureType;
    public final ArrayList<DayOfWeek> daysAvailable;

    public Feature(FeatureType featureType, DayOfWeek dayAvailable) {
        this.featureType = featureType;
        this.daysAvailable = new ArrayList<>();
        this.daysAvailable.add(dayAvailable);
    }

    @Override
    public String toString() {
        if (daysAvailable.size() == 7) {
            return this.featureType.toString();
        }
        return this.featureType.toString() + " only on " + this.daysAvailable.stream().map(dayOfWeek -> dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).collect(Collectors.joining(", "));
    }


}
