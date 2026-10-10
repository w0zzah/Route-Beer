package seng202.team5.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.exceptions.InvalidParametersException;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.RouteFormat;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.Location;
import seng202.team5.models.types.Theme;
import seng202.team5.services.route_generation.BarChoosingAlgorithm;

import java.security.InvalidParameterException;
import java.util.*;

import static seng202.team5.exceptions.InvalidParametersReason.*;

public class RouteService {
    private static final Logger logger = LogManager.getLogger(RouteService.class);


    /**
     * Generate a <b><i>single</i></b> new Route using user given parameter and the BarChoosingAlgorithm, assign into mainManager.currentRoute
     * @param RouteFormat mainManager containing all available bars
     */
    public static void generateRoute(MainManager mainManager, RouteFormat RouteFormat) {
        generateRoutes(mainManager, RouteFormat, 1);
    }

    /**
     * Generate a number of new Routes using user given parameter and the BarChoosingAlgorithm, assign into mainManager.currentRoute
     * @param RouteFormat mainManager containing all available bars
     * @param numberOfRoutes how many routes to generate
     */
    public static void generateRoutes(MainManager mainManager, RouteFormat RouteFormat, int numberOfRoutes) {
        checkParametersValid(mainManager, RouteFormat);
        mainManager.clearGeneratedRoutes();
        double MAX_DURATION_SECONDS = 3.5;
        double endTime = System.currentTimeMillis() + MAX_DURATION_SECONDS * 1000;
        int attemptNumber = 1;
        int generatedRouteIndex = 0;
        String lastReason = "The parameters are too strict";
        while (System.currentTimeMillis() < endTime && generatedRouteIndex < numberOfRoutes) {
            logger.info("Generating Route (Attempt {})", attemptNumber++);
            try {
                BarChoosingAlgorithm alg = new BarChoosingAlgorithm.Builder(mainManager.locationManager.findBarByName(RouteFormat.startBarInput()), mainManager.locationManager.getBars())
                        .endBar(mainManager.locationManager.findBarByName(RouteFormat.endBarInput()))
                        .numberOfBars(RouteFormat.numberOfBarsInput())
                        .approximateDistanceInKm(RouteFormat.approxDistanceInKmTextInput())
                        .themes(RouteFormat.themesInput())
                        .features(RouteFormat.featuresInput())
                        .particularBars(RouteFormat.particularBarsInput())
                        .build();
                mainManager.addGeneratedRoute(alg.getRoute());
                generatedRouteIndex++;
            } catch (NoRouteGeneratedException exception) {
                lastReason = exception.getMessage();
            }
        }
        if (generatedRouteIndex == 0) {
            throw new NoRouteGeneratedException(lastReason);
        }
        mainManager.setCurrentRoute(mainManager.getGeneratedRoute(0));
    }

    private static void checkParametersValid(MainManager mainManager, RouteFormat RouteFormat) {

        Bar startBar;
        Bar endBar = null;

        if (!RouteFormat.startBarInput().isEmpty()) {
            startBar = mainManager.locationManager.findBarByName(RouteFormat.startBarInput());
            if (startBar == null) throw new InvalidParametersException(START_BAR_DOESNT_EXIST);
        } else throw new InvalidParametersException(START_BAR_EMPTY);

        if  (!RouteFormat.endBarInput().isEmpty()) {
            endBar = mainManager.locationManager.findBarByName(RouteFormat.endBarInput());
            if (endBar == null) throw new InvalidParametersException(END_BAR_DOESNT_EXIST);
        }

        int maxNumberOfBars = RouteFormat.numberOfBarsInput() >= 2 ? RouteFormat.numberOfBarsInput() : 9;

        Set<Bar> requiredBars = new HashSet<>(RouteFormat.particularBarsInput());
        requiredBars.add(startBar);
        if (endBar != null) requiredBars.add(endBar);
        if (requiredBars.size() > maxNumberOfBars) {
            throw new InvalidParametersException(TOO_MANY_PARTICULAR_BARS);
        }

        Set<Theme> requiredThemes = new HashSet<>(RouteFormat.themesInput());
        requiredThemes.add(startBar.getTheme());
        if (endBar != null) requiredThemes.add(endBar.getTheme());
        if (requiredThemes.size() > maxNumberOfBars) {
            throw new InvalidParametersException(TOO_MANY_THEMES);
        }
    }

    /**
     * Find the closest amenity locations to the Route. Prioritises amenities to be spread out and later in the route
     * @param numLocations number of locations to add to route, must be less than number of bars
     * @param amenitiesNearby ? extends Location containing one type of amenity to find nearby
     * @param bars bars in the route to check for nearness
     * @return List of FastFood locations near to route
     */
    public static List<Location> getClosestAmenities(int numLocations, List<? extends Location> amenitiesNearby, List<Bar> bars) {
        if (numLocations < 0) {
            throw new InvalidParameterException("Number of locations must not be less than zero");
        }
        if (numLocations == 0) {
            return List.of();
        }

        int barSize = bars.size();

        List<double[]> estimatedFastFoodLocations = getEstimatedLocationsForAmenities(Math.min(numLocations, barSize - 1), bars);
        Set<? super Location> outputFastFoods = new HashSet<>();
        for (double[] latLong : estimatedFastFoodLocations) {
            amenitiesNearby.stream()
                    .min(Comparator.comparingDouble(
                    fastFood -> Math.pow(latLong[0] - fastFood.getLatitude(), 2) +
                                        Math.pow(latLong[1] - fastFood.getLongitude(), 2)
            )).ifPresent(outputFastFoods::add);
        }
        return (List<Location>) outputFastFoods.stream().toList();
    }

    /**
     * Gets the lat,lng of where amenities alongside the route should be placed
     * @param numLocations number of a particular amenity to place on the route
     * @param bars List of Bars in the route
     * @return List of {lat,lng} double arrays showing estimated locations of amenities
     */
    private static List<double[]> getEstimatedLocationsForAmenities(int numLocations, List<Bar> bars) {
        int barSize = bars.size();
        List<double[]> estimatedLocations = new ArrayList<>();
        for (int i = 1; i <= numLocations; i++) {
            estimatedLocations.add(
                    new double[]{
                            (bars.get(barSize-i-1).getLatitude() + bars.get(barSize-i).getLatitude()) / 2,
                            (bars.get(barSize-i-1).getLongitude() + bars.get(barSize-i).getLongitude()) / 2
                    }
            );
        }
        return estimatedLocations;
    }

}
