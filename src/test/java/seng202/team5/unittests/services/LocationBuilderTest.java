package seng202.team5.unittests.services;

import com.google.gson.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import seng202.team5.models.*;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.CCCLocation;
import seng202.team5.models.locations.Location;
import seng202.team5.services.route_generation.LocationBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LocationBuilderTest {

    private LocationBuilder builder;

    @BeforeEach
    public void setup() {
        builder = new LocationBuilder();
    }

    @Test
    public void testGetJsonObject() throws IOException {
        JsonArray jsonArray = (JsonArray) builder.getJsonObject("/json/format/correctFormatLocations.json");
        assertEquals(1, jsonArray.size());
        assertEquals(4, ((JsonObject) jsonArray.get(0)).size());
    }

    @Test
    public void testGetEmptyJsonObject() throws IOException {
        JsonObject jsonObject = (JsonObject) builder.getJsonObject("/json/input/empty.json");
        assertEquals("{}", jsonObject.toString());
    }


    @Test
    public void testThrowsNullPointerException() throws IOException {
        assertThrows(NullPointerException.class, () -> builder.getJsonObject("/resources/doesNotExist.json"));
    }

    @Nested
    class BuildLocationTest {

        JsonArray jsonArray;
        JsonObject object1;

        @BeforeEach
        void setup() {
            jsonArray = new JsonArray();
            object1 = new JsonObject();
            object1.addProperty("locID", "34369715");
            object1.addProperty("name", "Bush Inn Tavern");
            object1.addProperty("latitude", 172.574334);
            object1.addProperty("longitude", -43.5311462);
            jsonArray.add(object1);
        }

        @Test
        public void testBuildLocationsEmpty() {
            ArrayList<Location> locations = builder.buildLocations(new JsonArray(), Location.class);
            assertEquals(0, locations.size());
        }

        @Test
        public void testBuildLocationsClass() {
            ArrayList<Location> locations = builder.buildLocations(jsonArray, Location.class);

            assertEquals(Location.class, locations.getFirst().getClass());
        }

        @Test
        public void testBuildLocationsSingle() {
            ArrayList<Location> locations = builder.buildLocations(jsonArray, Location.class);

            assertEquals(1, locations.size());
        }

        @Test
        public void testBuildLocationsSingleFormat() {
            ArrayList<Location> locations = builder.buildLocations(jsonArray, Location.class);

            assertEquals(locations.getFirst().getLocID(), object1.get("locID").getAsString());
            assertEquals(locations.getFirst().getName(), object1.get("name").getAsString());
            assertEquals(locations.getFirst().getLatitude(), object1.get("latitude").getAsDouble(), 0);
            assertEquals(locations.getFirst().getLongitude(), object1.get("longitude").getAsDouble(), 0);
        }
    }

    @Nested
    class BuildBarLocationTest {
        JsonArray jsonArray;
        JsonObject object1;

        @BeforeEach
        void setup() {
            jsonArray = new JsonArray();

            object1 = new JsonObject();
            object1.addProperty("locID", "way/34369715");
            object1.addProperty("name", "Bush Inn Tavern");
            object1.addProperty("theme", "SPORTS_BAR");
            object1.addProperty("description", "A casual, friendly local pub featuring a relaxed sports bar vibe...");
            object1.addProperty("latitude", -43.5311462);
            object1.addProperty("longitude", 172.574334);
            object1.addProperty("address", "364 Riccarton Road, Upper Riccarton");
            object1.add("openingHours", null);

            JsonArray featureArray = new JsonArray();

            JsonObject feature1 = new JsonObject();
            feature1.addProperty("featureType", "POOL_TABLE");

            JsonArray daysForPool = new JsonArray();
            daysForPool.add("MONDAY");
            daysForPool.add("TUESDAY");
            daysForPool.add("WEDNESDAY");
            daysForPool.add("THURSDAY");
            daysForPool.add("FRIDAY");
            daysForPool.add("SATURDAY");
            daysForPool.add("SUNDAY");
            feature1.add("daysAvailable", daysForPool);

            featureArray.add(feature1);

            object1.add("features", featureArray);

            jsonArray.add(object1);

        }

        @Test
        public void testBuildBarsEmpty() {
            List<? extends Location> locations = builder.buildLocations(new JsonArray(), Bar.class);
            assertEquals(0, locations.size());
        }

        @Test
        public void testBuildBarsClass() {
            List<? extends Location> locations = builder.buildLocations(jsonArray, Bar.class);
            assertEquals(Bar.class, locations.getFirst().getClass());
        }

        @Test
        public void testBuildBarsSingle() {
            ArrayList<? extends Location> locations = builder.buildLocations(jsonArray, Bar.class);
            assertEquals(1, locations.size());
        }

        @Test
        public void testBuildBarsSingleFormat() {
            ArrayList<? extends Location> locations = builder.buildLocations(jsonArray, Bar.class);

            Bar bar = (Bar) locations.getFirst();

            assertEquals(bar.getLocID(), object1.get("locID").getAsString());
            assertEquals(bar.getName(), object1.get("name").getAsString());
            assertEquals(bar.getDescription(), object1.get("description").getAsString());
            assertEquals(bar.getLatitude(), object1.get("latitude").getAsDouble(), 0);
            assertEquals(bar.getLongitude(), object1.get("longitude").getAsDouble(), 0);
        }

    }

    @Nested
    class OpenStreetMapLocationTest {
        JsonObject jsonObject;
        JsonObject object1;
        JsonArray jsonArray;

        @BeforeEach
        void setup() {
            jsonObject = new JsonObject();
            jsonObject.addProperty("type", "FeatureCollection");
            jsonObject.addProperty("generator", "overpass-turbo");
            jsonObject.addProperty("copyright", "The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.");
            jsonObject.addProperty("timestamp", "2026-08-04T00:13:51Z");

            object1 = new JsonObject();
            object1.addProperty("type", "Feature");
            object1.add("properties", new JsonArray());
            object1.add("geometry", new JsonArray());
            object1.addProperty("id", "way/12345678");

            jsonArray = new JsonArray();
            jsonArray.add(object1);
        }

        @Test
        public void testBuildOpenStreetMapLocationsEmpty() {
            jsonObject.add("features", new JsonArray());

            ArrayList<OpenStreetMapLocation> locations = builder.buildOpenStreetMapLocations(jsonObject);
            assertEquals(0, locations.size());
        }

        @Test
        public void testBuildOpenStreetMapLocationsSingle() {
            jsonObject.add("features", jsonArray);

            ArrayList<OpenStreetMapLocation> locations = builder.buildOpenStreetMapLocations(jsonObject);
            assertEquals(1, locations.size());
        }

        @Test
        public void testBuildOpenStreetMapLocationsSingleFormat() {
            jsonObject.add("features", jsonArray);

            ArrayList<OpenStreetMapLocation> locations = builder.buildOpenStreetMapLocations(jsonObject);
            OpenStreetMapLocation openStreetMapLocation = locations.getFirst();

            assertEquals(openStreetMapLocation.id, object1.get("id").getAsString());
            assertEquals(openStreetMapLocation.type, object1.get("type").getAsString());
            assertInstanceOf(Map.class, openStreetMapLocation.geometry);
            assertInstanceOf(Map.class, openStreetMapLocation.properties);
        }
    }

    @Nested
    class CCCLocationTest {

        JsonObject jsonObject;
        JsonObject object1;
        JsonArray jsonArray;

        @BeforeEach
        void setup() {
            jsonObject = new JsonObject();
            jsonObject.addProperty("type", "FeatureCollection");

            object1 = new JsonObject();
            object1.addProperty("type", "Feature");
            object1.add("properties", new JsonArray());
            object1.add("geometry", new JsonArray());

            jsonArray = new JsonArray();
            jsonArray.add(object1);
        }

        @Test
        public void testBuildCCCLocationsEmpty() {
            jsonObject.add("features", new JsonArray());

            assertEquals(0, builder.buildCCCLocations(jsonObject).size());
        }

        @Test
        public void testBuildCCCLocationsSingle() {
            jsonObject.add("features", jsonArray);

            assertEquals(1, builder.buildCCCLocations(jsonObject).size());
        }

        @Test
        public void testBuildCCCLocationsSingleFormat() {
            jsonObject.add("features", jsonArray);

            ArrayList<CCCLocation> locations = builder.buildCCCLocations(jsonObject);
            CCCLocation cccLocation = locations.getFirst();

            assertEquals(cccLocation.type, object1.get("type").getAsString());
            assertInstanceOf(Map.class, cccLocation.geometry);
            assertInstanceOf(Map.class, cccLocation.properties);
        }
    }


    @Test
    public void testBuildBarJson() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputBars.json");
        Files.deleteIfExists(outputPath);
        builder.buildBarsJsonFromInputJsons(
                "/test/resources/json/output/outputBars.json",
                "/json/input/openStreetMapBars.geojson",
                "/json/input/openStreetMapRestaurants.geojson");
        assertTrue(Files.exists(outputPath));
        assertDoesNotThrow(() -> {JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();});
    }

    @Test
    public void testBuildBarJsonFormat() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputBars.json");
        Files.deleteIfExists(outputPath);
        builder.buildBarsJsonFromInputJsons(
                "/test/resources/json/output/outputBars.json",
                "/json/input/openStreetMapBars.geojson",
                "/json/input/openStreetMapRestaurants.geojson");

        JsonArray output = JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();

        assertDoesNotThrow(() -> {JsonObject a = output.get(0).getAsJsonObject();});
        JsonObject bar0 = output.get(0).getAsJsonObject();
        assertEquals("way/34369715", bar0.get("locID").getAsString());
        assertEquals("Bush Inn Tavern", bar0.get("name").getAsString());
        assertEquals(-43.5311462, bar0.get("latitude").getAsDouble());
        assertEquals(172.574334, bar0.get("longitude").getAsDouble());
    }

    @Test
    public void testBuildFeaturesFromCCCLocationJson() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputDrinkingFountains.json");
        Files.deleteIfExists(outputPath);
        builder.buildLocationsJsonFromCCCLocationsJson(
                "/test/resources/json/output/outputDrinkingFountains.json",
                "/json/input/CCCDrinkingFountains.geojson");
        assertTrue(Files.exists(outputPath));
        assertDoesNotThrow(() -> {JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();});
    }

    @Test
    public void testBuildFeaturesFromCCCLocationJsonFormat() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputDrinkingFountains.json");
        Files.deleteIfExists(outputPath);
        builder.buildLocationsJsonFromCCCLocationsJson(
                "/test/resources/json/output/outputDrinkingFountains.json",
                "/json/input/CCCDrinkingFountains.geojson");

        JsonArray output = JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();

        assertDoesNotThrow(() -> {JsonObject a = output.get(0).getAsJsonObject();});
        JsonObject drinkingFountain0 = output.get(0).getAsJsonObject();
        assertEquals("Corsair Bay Reserve", drinkingFountain0.get("name").getAsString());
        assertEquals(172.7000802317508, drinkingFountain0.get("longitude").getAsDouble());
        assertEquals(-43.60739776579255, drinkingFountain0.get("latitude").getAsDouble());
    }

    @Test
    public void testBuildFastFoodFromInputJson() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputFastFood.json");
        Files.deleteIfExists(outputPath);
        builder.buildFastFoodFromInputJson(
                "/test/resources/json/output/outputFastFood.json",
                "/json/input/openStreetMapFastFood.geojson");
        assertTrue(Files.exists(outputPath));
        assertDoesNotThrow(() -> {JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();});
    }

    @Test
    public void testBuildFastFoodFromInputJsonFormat() throws IOException {
        Path outputPath = Path.of("src/test/resources/json/output/outputFastFood.json");
        Files.deleteIfExists(outputPath);
        builder.buildFastFoodFromInputJson(
                "/test/resources/json/output/outputFastFood.json",
                "/json/input/openStreetMapFastFood.geojson");
        JsonArray output = JsonParser.parseString(Files.readString(outputPath)).getAsJsonArray();

        assertDoesNotThrow(() -> {
            output.get(0).getAsJsonObject();
        });
        JsonObject fastFood0 = output.get(0).getAsJsonObject();
        assertEquals("McDonald's", fastFood0.get("name").getAsString());
        assertEquals(172.5960812, fastFood0.get("longitude").getAsDouble());
        assertEquals(-43.5301811,  fastFood0.get("latitude").getAsDouble());
        assertEquals("155 Riccarton Road, Riccarton", fastFood0.get("address").getAsString());
        JsonArray openingHours = new JsonArray();
        openingHours.add(LocalTime.of(0,0).toString());
        openingHours.add(LocalTime.of(0,0).toString());
        assertEquals(openingHours, fastFood0.get("openingHours").getAsJsonObject().get("MONDAY").getAsJsonArray());

    }

    @Nested
    public class testPrivateDateTimeFormatting {

        @Nested
        public class testGetCoordsFromGeometryMap {
            static Method getCoordsFromGeometryMap;
            Map<String, Object> geometryMap = new HashMap<>();

            @BeforeAll
            public static void setup() throws NoSuchMethodException {
                getCoordsFromGeometryMap = LocationBuilder.class.getDeclaredMethod(
                        "getCoordsFromGeometryMap",
                        Map.class);
                getCoordsFromGeometryMap.setAccessible(true);
            }

            @Test
            public void testGetCoordsFromPoint() throws InvocationTargetException, IllegalAccessException {
                geometryMap.put("type", "Point");
                geometryMap.put("coordinates", new ArrayList<>(List.of(
                        0.0, 0.0
                )));
                ArrayList<Double> coords = (ArrayList<Double>) getCoordsFromGeometryMap.invoke(builder, geometryMap);
                assertEquals(0, coords.get(0));
                assertEquals(0, coords.get(1));
            }
            @Test
            public void testGetCoordsFromPolygon() throws InvocationTargetException, IllegalAccessException {
                ArrayList<ArrayList<Double>> polygon = new ArrayList<>(List.of(
                        new ArrayList<>(List.of(
                                0.0, 0.0
                        )),
                        new ArrayList<>(List.of(
                                1.0, 1.0
                        ))
                ));
                geometryMap.put("type", "Polygon");
                geometryMap.put("coordinates", new ArrayList<>(List.of(polygon)));
                ArrayList<Double> coords = (ArrayList<Double>) getCoordsFromGeometryMap.invoke(builder, geometryMap);
                assertEquals(0, coords.get(0));
                assertEquals(0, coords.get(1));
            }
        }

        @Nested
        public class testGetAddressFromPropertiesMap {
            static Method getAddressFromPropertiesMap;
            Map<String, Object> propertiesMap = new HashMap<>();

            @BeforeAll
            public static void setUp() throws NoSuchMethodException {
                getAddressFromPropertiesMap = LocationBuilder.class.getDeclaredMethod(
                        "getAddressFromPropertiesMap",
                        Map.class);
                getAddressFromPropertiesMap.setAccessible(true);
            }

            @Test
            public void testGetAddressFromPropertiesMapEmpty() throws InvocationTargetException, IllegalAccessException {
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertNull(testAddress);
            }
            
            @Test
            public void testGetAddressFromPropertiesMapNumber() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:housenumber", "1");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertNull(testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapStreet() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:street", "Main Street");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("Main Street", testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapNumberStreet() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:housenumber", "1");
                propertiesMap.put("addr:street", "Main Street");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("1 Main Street", testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapSuburb() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:suburb", "City Centre");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("City Centre", testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapNumberSuburb() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:housenumber", "1");
                propertiesMap.put("addr:suburb", "City Centre");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("City Centre", testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapStreetSuburb() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:street", "Main Street");
                propertiesMap.put("addr:suburb", "City Centre");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("Main Street, City Centre", testAddress);
            }
            @Test
            public void testGetAddressFromPropertiesMapNumberStreetSuburb() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("addr:housenumber", "1");
                propertiesMap.put("addr:street", "Main Street");
                propertiesMap.put("addr:suburb", "City Centre");
                String testAddress = (String) getAddressFromPropertiesMap.invoke(builder, propertiesMap);
                assertEquals("1 Main Street, City Centre", testAddress);
            }
        }

        @Nested
        public class testGetOpeningHoursFromPropertiesMap {
            static Method getOpeningHoursFromPropertiesMap;
            Map<String, Object> propertiesMap = new HashMap<>();

            @BeforeAll
            public static void setUp() throws NoSuchMethodException {
                getOpeningHoursFromPropertiesMap = LocationBuilder.class.getDeclaredMethod(
                        "getOpeningHoursFromPropertiesMap",
                        Map.class);
                getOpeningHoursFromPropertiesMap.setAccessible(true);
            }

            @Test
            public void testGetOpeningHoursEmpty() throws InvocationTargetException, IllegalAccessException {
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);
                assertNull(openingHours);
            }

            @Test
            public void testGetOpeningHoursNoDigits() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "off");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);
                assertNull(openingHours);
            }

            @Test
            public void testGetOpeningHoursOpenToClose() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0)));
                for (DayOfWeek day : DayOfWeek.values()) {
                    assertEquals(expected, openingHours.get(day));
                }
            }

            @Test
            public void testGetOpeningHours247() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "24/7");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(0, 0), LocalTime.of(0, 0)));
                for (DayOfWeek day : DayOfWeek.values()) {
                    assertEquals(expected, openingHours.get(day));
                }
            }

            @Test
            public void testGetOpeningHoursOpenToLate() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "20:00+");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(20, 0), LocalTime.of(11, 59)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.SUNDAY));
            }

            @Test
            public void testGetOpeningHoursOpenToCloseDaysDashed() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo-Fr 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.FRIDAY));
                assertNull(openingHours.get(DayOfWeek.SATURDAY));
                assertNull(openingHours.get(DayOfWeek.SUNDAY));
            }

            @Test
            public void testGetOpeningHoursSemicolonSeparated() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo-Fr 09:00-17:00; Sa-Su 10:00-15:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                assertEquals(new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0))),
                        openingHours.get(DayOfWeek.WEDNESDAY));
                assertEquals(new ArrayList<>(List.of(LocalTime.of(10, 0), LocalTime.of(15, 0))),
                        openingHours.get(DayOfWeek.SATURDAY));
            }

            @Test
            public void testGetOpeningHoursCommaSeparated() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "We 09:00-17:00, Th 10:00-16:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                assertEquals(new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0))),
                        openingHours.get(DayOfWeek.WEDNESDAY));
                assertEquals(new ArrayList<>(List.of(LocalTime.of(10, 0), LocalTime.of(16, 0))),
                        openingHours.get(DayOfWeek.THURSDAY));
                assertNull(openingHours.get(DayOfWeek.SATURDAY));
            }

            @Test
            public void testGetOpeningHoursSingleDay() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                assertEquals(1, openingHours.size());
                assertEquals(new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0))),
                        openingHours.get(DayOfWeek.MONDAY));
            }

            @Test
            public void testGetOpeningHoursPublicHoliday() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "PH 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                assertEquals(0, openingHours.size());
            }
            @Test
            public void testGetOpeningHoursDayOff() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo off, Tu 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                assertTrue(openingHours.containsKey(DayOfWeek.MONDAY));
                assertNull(openingHours.get(DayOfWeek.MONDAY));
                assertEquals(new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0))),
                        openingHours.get(DayOfWeek.TUESDAY));
            }
            @Test
            public void testGetOpeningHoursOpenToCloseLate() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "18:00-23:00+");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(18, 0), LocalTime.of(11, 59)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.SUNDAY));
            }

            @Test
            public void testGetOpeningHoursMultipleHoursCollapses() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "09:00-12:00,13:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                // the lunch-break gap (12:00-13:00) is dropped: implementation only keeps first open + last close
                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
            }

            @Test
            public void testGetOpeningHoursMultipleHoursToLateCollapses() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo 09:00-12:00,13:00-17:00+");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(11, 59)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(1, openingHours.size());
            }

            @Test
            public void testGetOpeningHoursInvalidFormat() {
                propertiesMap.put("opening_hours", "9-17");
                InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                        () -> getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap));

                assertInstanceOf(IllegalArgumentException.class, exception.getCause());
                assertTrue(exception.getCause().getMessage().contains("hoursString format error"));
            }

            @Test
            public void testGetOpeningHoursDaysCommaSeparated() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo,We,Fr 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.WEDNESDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.FRIDAY));
                assertNull(openingHours.get(DayOfWeek.TUESDAY));
                assertNull(openingHours.get(DayOfWeek.THURSDAY));
                assertEquals(3, openingHours.size());
            }

            @Test
            public void testGetOpeningHoursDaysCommaAndDashSeparated() throws InvocationTargetException, IllegalAccessException {
                propertiesMap.put("opening_hours", "Mo-We,Fr 09:00-17:00");
                Map<DayOfWeek, ArrayList<LocalTime>> openingHours =
                        (Map<DayOfWeek, ArrayList<LocalTime>>) getOpeningHoursFromPropertiesMap.invoke(builder, propertiesMap);

                ArrayList<LocalTime> expected = new ArrayList<>(List.of(LocalTime.of(9, 0), LocalTime.of(17, 0)));
                assertEquals(expected, openingHours.get(DayOfWeek.MONDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.TUESDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.WEDNESDAY));
                assertEquals(expected, openingHours.get(DayOfWeek.FRIDAY));
                assertNull(openingHours.get(DayOfWeek.THURSDAY));
                assertNull(openingHours.get(DayOfWeek.SATURDAY));
            }
        }
    }
}
