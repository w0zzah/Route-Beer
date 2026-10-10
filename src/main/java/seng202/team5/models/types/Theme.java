package seng202.team5.models.types;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enum for bar themes
 */
public enum Theme {

    IRISH_PUB,
    CRAFT_BREWERY,
    WINE_BAR,
    COCKTAIL_BAR,
    NIGHTCLUB,
    SPORTS_BAR,
    ROOFTOP_BAR,
    ARCADE_BAR,
    BOWLING_CLUB,
    SOCIAL_CLUB,
    RESTAURANT_BAR,
    TRADITIONAL_TAVERN,
    AIRPORT_LOUNGE,
    NEIGHBOURHOOD_PUB;

    @Override
    public String toString()
    {
        return Pattern.compile("_")
                .splitAsStream(this.name())
                .map(word ->
                        word.substring(0,1).toUpperCase() +
                                word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
