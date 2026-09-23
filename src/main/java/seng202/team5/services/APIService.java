package seng202.team5.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import seng202.team5.models.Bar;
import seng202.team5.models.DistanceMatrix;
import seng202.team5.models.Location;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides methods for interacting with the geoapify API
 * This includes retrieving addresses, distances matrices,
 * and location search results
 */
public class APIService {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String API_KEY = dotenv.get("API_KEY");
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     *  Gets the formatted address of a location from its latitude and
     *  longitude, using the geoapify API
     * @param latitude location latitude
     * @param longitude location longitude
     * @return the formatted address of the location
     */
    public static String getAddressFromCoords(double latitude, double longitude) {

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.geoapify.com/v1/geocode/reverse?" +
                        "lat=" + latitude +
                        "&lon=" + longitude +
                        "&apiKey=" + API_KEY))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
            }

            JsonObject returnJson = JsonParser.parseString(response.body()).getAsJsonObject();
            if (returnJson.get("features").getAsJsonArray().isEmpty()) {
                return null;
            }

            return returnJson
                    .get("features").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("properties").getAsJsonObject()
                    .get("formatted").getAsString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls from Geoapify to get matrix of distances from one location to another<br>
     * Geoapify has a soft cap at 3000 credits per day (which this will exceed for )
     * @param locations ArrayList of locations to calculate walking distances between
     * @return DistanceMatrix containing all bar location-pairs in location matrix
     */
    public static DistanceMatrix getDistancesMatrix(ArrayList<? extends Location> locations) {
        if (locations == null || locations.size() < 2) {
            throw new IllegalArgumentException("locations mustn't be null and must have at least 2 locations.");
        }

        int MAX_REQUEST_PER_CALL = 1000; // Geoapify conditions
        int numLocations = locations.size(); // 150ish
        int perLoop = MAX_REQUEST_PER_CALL / numLocations; // 6ish
        int currentIndex = 0;

        List<String> locIDs = locations.stream().map(Location::getLocID).toList();

        String locationsJsonArrayElementsAll = locations
                .stream()
                .map(Location::getLocationForGeoapify)
                .collect(Collectors.joining(", "));

        ArrayList<ArrayList<Integer>> distances = new ArrayList<>();

        while (currentIndex < numLocations) {
            int startIndex = currentIndex;
            int endIndex = Math.min(currentIndex + perLoop, numLocations);

            String locationsJsonArrayElementsLoop = locations
                    .subList(startIndex, endIndex)
                    .stream()
                    .map(Location::getLocationForGeoapify)
                    .collect(Collectors.joining(", "));

            String jsonPayload = "{" +
                    "\"mode\": \"walk\"," +
                    "\"sources\": [" + locationsJsonArrayElementsLoop + "]," +
                    "\"targets\": [" + locationsJsonArrayElementsAll + "]" +
                    "}";
            HttpRequest httpRequest = HttpRequest
                    .newBuilder()
                    .uri(URI.create("https://api.geoapify.com/v1/routematrix?apiKey=" + API_KEY
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                JsonArray sourceToTargetMatrix = JsonParser
                        .parseString(response.body()).getAsJsonObject()
                        .get("sources_to_targets").getAsJsonArray();

                int rows = sourceToTargetMatrix.size();
                int cols = ((JsonArray) sourceToTargetMatrix.get(0)).size();
                for (int i = 0; i < rows; i++) {
                    ArrayList<Integer> row = new ArrayList<>();
                    for (int j = 0; j < cols; j++) {
                        row.add(j, sourceToTargetMatrix
                                .get(i).getAsJsonArray()
                                .get(j).getAsJsonObject()
                                .get("distance").getAsInt());
                    }
                    distances.add(row);
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            currentIndex += perLoop;
        }

        return new DistanceMatrix(distances, locIDs);

    }

}
