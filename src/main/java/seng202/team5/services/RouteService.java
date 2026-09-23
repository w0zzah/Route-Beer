package seng202.team5.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.*;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class RouteService {
    /**
     * Generate a new Route using user given parameter and the BarChoosingAlgorithm, assign into mainManager.currentRoute
     * @param mainManager mainManager containing all available bars
     * @param startBar bar to begin at, compulsory or BarChoosingAlgorithm throws NoStartBarException
     * @param endBar bar to finish the route at
     * @param numberOfBars number of bars to visit
     * @param approximateDistanceInKm approximate distance the route will travel
     * @param themes any bar themes the Route must visit
     * @param features any bar features the Route must include
     */
    private static final Logger logger = LogManager.getLogger(RouteService.class);
    public static void generateRoute(MainManager mainManager, Bar startBar, Bar endBar, int numberOfBars, Double approximateDistanceInKm, List<Theme> themes, List<FeatureType> features) {
        double MAX_DURATION_SECONDS = 3.5;
        double endTime = System.currentTimeMillis() + MAX_DURATION_SECONDS * 1000;
        int attemptNumber = 1;
        String lastReason = "The parameters are too strict";
        while (System.currentTimeMillis() < endTime) {
            logger.info("Generating Route (Attempt {})", attemptNumber++);
            try {
                BarChoosingAlgorithm alg = new BarChoosingAlgorithm.Builder(startBar, mainManager.locationManager.getBars())
                        .endBar(endBar)
                        .numberOfBars(numberOfBars)
                        .approximateDistanceInKm(approximateDistanceInKm)
                        .themes(themes)
                        .features(features)
                        .build();

                mainManager.setCurrentRoute(alg.getRoute());
                return;
            } catch (IOException exception) {
                logger.error("Error loading files");
            } catch (NoRouteGeneratedException exception) {
                lastReason = exception.getMessage();
            }
        }
        throw new NoRouteGeneratedException(lastReason);
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
