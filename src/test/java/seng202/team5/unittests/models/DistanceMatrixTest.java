package seng202.team5.unittests.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.models.DistanceMatrix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DistanceMatrixTest {

    static ArrayList<ArrayList<Integer>> emptyLocationDistanceMatrixList;
    static ArrayList<ArrayList<Integer>> oneLocationDistanceMatrixList;
    static ArrayList<ArrayList<Integer>> twoLocationDistanceMatrixList;
    static ArrayList<ArrayList<Integer>> threeLocationDistanceMatrixList;
    static List<String> emptyLocIDs;
    static List<String> oneLocIDs;
    static List<String> twoLocIDs;
    static List<String> threeLocIDs;
    @BeforeAll
    public static void setUpAll() {
        emptyLocationDistanceMatrixList = new ArrayList<>();
        oneLocationDistanceMatrixList = new ArrayList<>(List.of(
                new ArrayList<>(List.of(0))));
        twoLocationDistanceMatrixList = new ArrayList<>(List.of(
                new ArrayList<>(List.of(  0, 100)),
                new ArrayList<>(List.of(100,   0))));
        threeLocationDistanceMatrixList = new ArrayList<>(List.of(
                new ArrayList<>(List.of(  0, 100,  50)),
                new ArrayList<>(List.of(100,   0,  40)),
                new ArrayList<>(List.of( 50,  40,   0))
        ));
        emptyLocIDs = new ArrayList<>();
        oneLocIDs = List.of("node/1");
        twoLocIDs = List.of("node/0", "node/1");
        threeLocIDs = List.of("node/0", "node/1", "node/2");
    }

    @Nested
    class ConstructorFromMatrixTests {

        @Test
        public void testConstructorForNonSquareMatrix() {
            ArrayList<ArrayList<Integer>> nonSquareMatrix = new ArrayList<>(List.of(new ArrayList<>(List.of(100, 200))));
            assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(nonSquareMatrix, oneLocIDs));
        }

        @Test
        public void testConstructorForMismatchedElements() {
            assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(oneLocationDistanceMatrixList, emptyLocIDs));
        }

        @Test
        public void testConstructorFromDataEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(emptyLocationDistanceMatrixList, emptyLocIDs));
        }

        @Test
        public void testConstructorFromDataOneBar() {
            assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(oneLocationDistanceMatrixList, oneLocIDs));
        }

        @Test
        public void testConstructorFromDataTwoBar() {
            DistanceMatrix distanceMatrix = new DistanceMatrix(twoLocationDistanceMatrixList, twoLocIDs);
            assertNotNull(distanceMatrix);
            assertEquals(2, distanceMatrix.getSize());
            assertEquals(100, distanceMatrix.getDistanceMetres("node/0", "node/1"));
            assertEquals(100, distanceMatrix.getDistanceMetres("node/1", "node/0"));
            assertEquals(0, distanceMatrix.getDistanceMetres("node/1", "node/1"));
        }
    }

    @Nested
    class BuildDistanceMatrixJsonTests {

        Path outputPath;

        @BeforeEach
        public void setUp() throws IOException {
            outputPath = Path.of("src/test/resources/json/distanceMatrices/distanceMatrixTestMatrix.json");
            Files.deleteIfExists(outputPath);
            DistanceMatrix distanceMatrix = new DistanceMatrix(twoLocationDistanceMatrixList, twoLocIDs);
            distanceMatrix.buildJsonMatrix("/test/resources/json/distanceMatrices/distanceMatrixTestMatrix.json");
        }

        @Test
        public void testBuildDistanceMatrixJson() {
            assertTrue(Files.exists(outputPath));
            assertDoesNotThrow(() -> JsonParser.parseString(Files.readString(outputPath)).getAsJsonObject());
        }

        @Test
        public void testBuildDistanceMatrixJsonFormat() throws IOException {
            JsonObject distancesMatrixJsonObject = JsonParser.parseString(Files.readString(outputPath)).getAsJsonObject();
            Gson gson = new Gson();
            Map<String, Integer> locIDsMap = gson.fromJson(distancesMatrixJsonObject.get("locIDs").getAsJsonObject(),
                    new TypeToken<Map<String, Integer>>(){}.getType());
            int[] distancesArray = gson.fromJson(distancesMatrixJsonObject.get("distances").getAsJsonArray(), int[].class);

            assertEquals(2, locIDsMap.size());
            assertEquals(1, distancesArray.length);
        }
    }

    @Nested
    class ConstructorFromJsonTests {

        @Test
        public void testConstructorFromJson() throws IOException {
            DistanceMatrix distanceMatrix = new DistanceMatrix("/json/distanceMatrices/twoBarDistanceMatrix.json");
            assertEquals(2, distanceMatrix.getSize());
            assertEquals(954, distanceMatrix.getDistanceMetres("node/0", "node/1"));
            assertEquals(954, distanceMatrix.getDistanceMetres("node/1", "node/0"));
            assertEquals(0, distanceMatrix.getDistanceMetres("node/1", "node/1"));
        }
    }

    @Nested
    public class testToString {

        DistanceMatrix threeLocationDistanceMatrix;
        DistanceMatrix twoLocationDistanceMatrix;

        @BeforeEach
        public void setUp() throws IOException {
            threeLocationDistanceMatrix = new DistanceMatrix(threeLocationDistanceMatrixList, threeLocIDs);
            twoLocationDistanceMatrix = new DistanceMatrix(twoLocationDistanceMatrixList, twoLocIDs);
        }

        @Test
        public void twoLocationDistanceMatrix() {
            String distanceMatrixString = twoLocationDistanceMatrix.toString();
            String expectedString =
                    "DistanceMatrix\n" +
                    "100\n" +
                    "size=2";
            assertEquals(expectedString, distanceMatrixString);
        }
        @Test
        public void threeLocationDistanceMatrix() {
            String distanceMatrixString = threeLocationDistanceMatrix.toString();
            String expectedString =
                    "DistanceMatrix\n" +
                    "100\n" +
                    "50, 40\n" +
                    "size=3";
            assertEquals(expectedString, distanceMatrixString);
        }

    }
}
