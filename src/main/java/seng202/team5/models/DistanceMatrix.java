package seng202.team5.models;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map;

/**
 * Used to store square, symmetrical, zero-diagonal array efficiently for algorithmic use
 */
public class DistanceMatrix {

    private final int[] flattenedDistanceArray;
    // size refers to original length of sides on matrix, not length of flattened array
    private final int size;
    private final Map<String, Integer> locIDMap;

    /**
     * Constructs a DistanceMatrix from a square matrix of distances
     * and a list of corresponding location IDs
     * @param matrixData the square matrix containing  distances between locations
     * @param locIDs the list of locIDs corresponding to the rows/cols of the matrix
     * @throws IllegalArgumentException if the matrix is empty, not square, does not
     *                                  match locID list size, or is of length one
     */
    public DistanceMatrix(ArrayList<ArrayList<Integer>> matrixData, List<String> locIDs) {
        if (matrixData.isEmpty()) {
            throw new IllegalArgumentException("Matrix data cannot be empty");
        } else if (matrixData.size() != matrixData.getFirst().size()) {
            throw new IllegalArgumentException(
                    "matrixData is not square! size = ("  + matrixData.size() + ", " + matrixData.getFirst().size() + ")");
        } else if (matrixData.size() != locIDs.size()) {
            throw new IllegalArgumentException("matrixData and locIDs are not the same size!" +
                    " matrixData = " + matrixData.size() +
                    ", locIDs =  " + locIDs.size());
        } else if (matrixData.size() == 1) {
            throw new IllegalArgumentException("DistanceMatrix cannot be made from single location! matrixData is size 1x1");
        }

        size = matrixData.size();
        locIDMap = new HashMap<>();
        for (int i = 0; i < locIDs.size(); i++) {
            locIDMap.put(locIDs.get(i), i);
        }
        flattenedDistanceArray = new int[size * (size-1) / 2];
        for (int row = 1; row < size; row++) {
            for (int col = 0; col < row; col++) {
                flattenedDistanceArray[getFlattenedIndex(row, col)] = matrixData.get(row).get(col);
            }
        }
    }

    /**
     * Creates a distance matrix by loading in stored data
     * from a JSON resource
     * @param resourcePath the path to the JSON file containing
     *                     the distances and locIDs
     */
    public DistanceMatrix(String resourcePath) {
        Gson gson = new Gson();

        JsonObject matrixJsonObject = JsonParser.parseReader(
                new InputStreamReader(Objects.requireNonNull(
                        getClass().getResourceAsStream(resourcePath)
                ), StandardCharsets.UTF_8)
        ).getAsJsonObject();

        flattenedDistanceArray = gson.fromJson(matrixJsonObject.get("distances"),  int[].class);
        locIDMap = gson.fromJson(matrixJsonObject.get("locIDs"),
                new TypeToken<Map<String, Integer>>(){}.getType());
        size = locIDMap.size();
    }

    /**
     * Get appropriate index in flattened matrix array from row col
     * @param row the row of the matrix
     * @param col the column of the matrix
     * @return index of flattened array
     */
    private int getFlattenedIndex(int row, int col) {
        return row * (row-1) / 2 + col;
    }

    /**
     * Get walking distance between bars stored in matrix from id's referring to index
     * @param sourceLocID locID of starting bar
     * @param targetLocID locID of end bar
     * @return int representing walking distance between in metres
     */
    public int getDistanceMetres(String sourceLocID, String targetLocID) {
        int sourceIndex =  locIDMap.get(sourceLocID);
        int targetIndex =  locIDMap.get(targetLocID);

        if (sourceIndex >= size || targetIndex >= size) {
            throw new IllegalArgumentException("One of the indices was too large for given matrix! (" + sourceIndex + ", " + targetIndex + ") for size " + size);
        }
        if (sourceIndex ==  targetIndex) {
            return 0;
        }
        int row = Math.max(sourceIndex, targetIndex);
        int col = Math.min(sourceIndex, targetIndex);
        return flattenedDistanceArray[getFlattenedIndex(row, col)];
    }
    public int getDistanceMetres(Bar sourceBar, Bar targetBar) {
        return getDistanceMetres(sourceBar.getLocID(), targetBar.getLocID());
    }
    public double getDistanceKm(String sourceLocID, String targetLocID) {
        double distanceInMetres = getDistanceMetres(sourceLocID, targetLocID);
        return (distanceInMetres / 1000);
    }
    public double getDistanceKm(Bar sourceBar, Bar targetBar) {
        return getDistanceKm(sourceBar.getLocID(), targetBar.getLocID());
    }

    /**
     * Create a JSON file of the matrix to store for later
     * @param outputJsonPath path from src e.g. "/main/resources/json/data/example.json"
     */
    public void buildJsonMatrix(String outputJsonPath) throws IOException {
        JsonArray distanceArray = new Gson().toJsonTree(flattenedDistanceArray).getAsJsonArray();
        JsonObject locIDMapObject = new Gson().toJsonTree(locIDMap).getAsJsonObject();

        JsonObject matrixJsonObject = new JsonObject();
        matrixJsonObject.add("distances", distanceArray);
        matrixJsonObject.add("locIDs", locIDMapObject);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        try (FileWriter fileWriter = new FileWriter("src" + outputJsonPath)) {
            gson.toJson(matrixJsonObject, fileWriter);
        }
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DistanceMatrix\n");

        if (size < 10) {
            for (int row = 1; row < size; row++) {
                for (int col = 0; col < row; col++) {
                    sb.append(flattenedDistanceArray[getFlattenedIndex(row, col)]);
                    if (col < row - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("\n");
            }
        }
        sb.append("size=").append(size);
        return sb.toString();
    }
}
