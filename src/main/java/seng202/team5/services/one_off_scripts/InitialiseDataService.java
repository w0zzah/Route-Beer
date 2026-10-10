package seng202.team5.services.one_off_scripts;

import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.DistanceMatrix;
import seng202.team5.services.route_generation.LocationBuilder;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to initialize all data for the application including:
 *
 *     Bar, FastFood, DrinkingFountain, and PublicToilet data from CCC and OpenStreetMap
 *     Route algorithm pre-calculated distance matrix from Geoapify
 *
 * This service is currently idle, but can be used in the future to properly initialize further data
 */
public class InitialiseDataService {

    /**
     * Run to build application location data, as more api data is acquired more arguments will need to be added
     */
    static void main() throws IOException {
        LocationBuilder builder = new LocationBuilder();
        String outputDirectory = "/main/resources/json/data/";

//        // build bars JSON
        System.out.println("Building Bars JSON...");
        builder.buildBarsJsonFromInputJsons(
                outputDirectory + "bars.json",
                "/json/chch-bars-pubs.geojson",
                "/json/chch-restaurants.geojson"
        );
        System.out.println("DONE");
        // build drinking fountain JSON
        System.out.println("Building Drinking Fountains JSON...");
        builder.buildLocationsJsonFromCCCLocationsJson(
                outputDirectory + "drinking-fountains.json",
                "/json/chch-drinking-fountains.geojson"
        );
        System.out.println("DONE");
        // build public toilets JSON
        System.out.println("Building Public Toilets JSON...");
        builder.buildLocationsJsonFromCCCLocationsJson(
                outputDirectory + "public-toilets.json",
                "/json/chch-public-toilets.geojson"
        );
        System.out.println("DONE");
        // build fast food locations JSON
        System.out.println("Building FastFood JSON...");
        builder.buildFastFoodFromInputJson(
                outputDirectory + "fast-food.json",
                "/json/chch-fast-food.geojson"
        );
        System.out.println("DONE");
        // build distance matrix between bars for route generation optimization
        System.out.println("Building Distance Matrix JSON...");
        ArrayList<Bar> bars = builder.buildLocations("/json/data/bars.json", Bar.class);
        System.out.println("Getting Distance Matrix Object from Locations JSON...");
        DistanceMatrix distancesMatrix = APIService.getDistancesMatrix(bars);
        distancesMatrix.buildJsonMatrix(outputDirectory + "distance-matrix.json");
        System.out.println("DONE");

    }
}
