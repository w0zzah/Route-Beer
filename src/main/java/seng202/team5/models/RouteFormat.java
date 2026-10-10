package seng202.team5.models;

import seng202.team5.models.locations.Bar;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;
import seng202.team5.services.RouteBuilderService;

import java.util.List;

/**
 * Format for a route to be checked by {@link RouteBuilderService}
 * @param startBarInput Name typed into the start bar field (cannot be null)
 * @param endBarInput Name typed into the end bar field (can be null)
 * @param numberOfBarsInput Requested number of bars (0 if unspecified)
 * @param approxDistanceInKmTextInput Approximate distance as typed
 * @param themesInput List of user selected Themes {@link Theme}
 * @param featuresInput List of user selected features {@link Feature}
 * @param particularBarsInput List of user selected Bars  {@link Bar}
 */



public record RouteFormat(
        String startBarInput,
        String endBarInput,
        int numberOfBarsInput,
        Double approxDistanceInKmTextInput,
        List<Theme> themesInput,
        List<FeatureType> featuresInput,
        List<Bar> particularBarsInput) {
}