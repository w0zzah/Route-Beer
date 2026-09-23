package seng202.team5.models;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Holds feature name and icon
 * @author Theo Baker
 */

public enum FeatureType {
    POOL_TABLE,
    DART_BOARD,
    KARAOKE,
    OUTDOOR_SEATING,
    LIVE_MUSIC,
    GAMING_MACHINES;

    public final String iconID;

    FeatureType() {
        this.iconID = this.name().toLowerCase().replaceAll("_", "-") + ".svg";
    }

    @Override
    public String toString() {
        return Pattern.compile("_")
                .splitAsStream(this.name())
                .map(word ->
                        word.substring(0,1).toUpperCase() +
                        word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
