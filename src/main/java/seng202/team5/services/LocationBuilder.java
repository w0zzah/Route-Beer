package seng202.team5.services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import seng202.team5.models.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder for all location java classes; Location is what we will use within the Application,
 * whereas others are used intermediarily for building final JSON
 */

public class LocationBuilder {

    private final Gson gson;
    private final LocalTime late = LocalTime.parse("11:59");

    public LocationBuilder() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
                .serializeNulls()
                .create();
    }

    /**
     * Get json object from path within resource package
     * @param resourcePath location of json file in resource package es.g. "/json/example.json"
     * @return Gson.JsonObject of json file
     */
    public JsonElement getJsonObject(String resourcePath) {
        return JsonParser.parseReader(
                new InputStreamReader(Objects.requireNonNull(
                        getClass().getResourceAsStream(resourcePath)
                ), StandardCharsets.UTF_8)
        );
    }

    /**
     * Can build location array of any location type specifically
     * @param jsonArray contains Locations in JsonArrayObject
     * @param locationClass the Extended Location to build from json array
     * @return ArrayList of Locations for use in end user map <b>of</b> type locationClass
     */
    public <T> ArrayList<T> buildLocations(JsonArray jsonArray, Class<T> locationClass) {
        Type arrayListType = TypeToken.getParameterized(ArrayList.class, locationClass).getType();
        return gson.fromJson(jsonArray, arrayListType);
    }

    /**
     * Overloaded constructor to build locations directly from resourcePath String
     * @param resourcePath location of json file in resource package e.g. "/json/example.json"
     * @param locationClass the Extended Location to build from json array
     * @return ArrayList of Locations for use in end user map <b>of</b> type locationClass
     */
    public <T> ArrayList<T> buildLocations(String resourcePath, Class<T> locationClass) {
        JsonArray locationsArray = (JsonArray) this.getJsonObject(resourcePath);
        return buildLocations(locationsArray, locationClass);
    }

    /**
     * Creates array of OpenStreetMapLocations used to build tidy json of locations
     * @param openStreetMapJsonObject of openstreetmap json data
     * @return array of OpenStreetMapLocations
     */
    public ArrayList<OpenStreetMapLocation> buildOpenStreetMapLocations(JsonObject openStreetMapJsonObject) {
        JsonArray jsonArray = openStreetMapJsonObject.getAsJsonArray("features");
        Type arrayListType = TypeToken.getParameterized(ArrayList.class, OpenStreetMapLocation.class).getType();
        return gson.fromJson(jsonArray, arrayListType);
    }

    /**
     * Creates array of CCCLocations used to build tidy JSON of locations
     * @param CCCJsonObject of Christchurch City Council JSON data
     * @return array of CCCLocations
     */
    public ArrayList<CCCLocation> buildCCCLocations(JsonObject CCCJsonObject) {
        JsonArray jsonArray = CCCJsonObject.getAsJsonArray("features");
        Type arrayListType = TypeToken.getParameterized(ArrayList.class, CCCLocation.class).getType();
        return gson.fromJson(jsonArray, arrayListType);
    }

    /**
     * Builds bars json for use in map
     * @param outputJsonPath path from src e.g. "/main/resources/example.json"
     * @param openStreetMapBarsJsonPath path from resources package e.g. "/json/example1.json"
     * @param openStreetMapRestaurantsJsonPath path from resources package e.g. "/json/example2.json"
     */
    public void buildBarsJsonFromInputJsons(String outputJsonPath,
                                            String openStreetMapBarsJsonPath,
                                            String openStreetMapRestaurantsJsonPath) throws IOException {
        JsonObject openStreetMapJsonObjectBar = (JsonObject) getJsonObject(openStreetMapBarsJsonPath);
        JsonObject openStreetMapJsonObjectRestaurant = (JsonObject) getJsonObject(openStreetMapRestaurantsJsonPath);

        ArrayList<OpenStreetMapLocation> openStreetMapBars = buildOpenStreetMapLocations(
                openStreetMapJsonObjectBar
        );
        ArrayList<OpenStreetMapLocation> openStreetMapRestaurants = BarFilter.filter(buildOpenStreetMapLocations(
                openStreetMapJsonObjectRestaurant
        ));
        ArrayList<OpenStreetMapLocation> openStreetMapLocations = Stream.concat(
                openStreetMapBars.stream(),
                openStreetMapRestaurants.stream()
        ).collect(Collectors.toCollection(ArrayList::new));

        JsonArray jsonArray = new JsonArray();

        for (OpenStreetMapLocation openStreetMapLocation : openStreetMapLocations) {
            // latitude and longitude
            ArrayList<Double> coords = getCoordsFromGeometryMap(openStreetMapLocation.geometry);
            double latitude = coords.get(1);
            double longitude = coords.get(0);
            // id
            String id = openStreetMapLocation.id;
            // name
            String name = (String) openStreetMapLocation.properties.get("name");
            // description
            String description = "";
            // address
            String rawAddress = Objects.requireNonNullElseGet(
                    getAddressFromPropertiesMap(openStreetMapLocation.properties),
                    () -> APIService.getAddressFromCoords(latitude, longitude));
            String address = rawAddress.startsWith(name + ", ") ? rawAddress.substring(name.length() + 2) : rawAddress;
            // opening times
            Map<DayOfWeek, ArrayList<LocalTime>> opening_hours = getOpeningHoursFromPropertiesMap(openStreetMapLocation.properties);
            // features
            ArrayList<Feature> features = null; // getFeaturesFromPropertiesMap(openStreetMapLocation.properties); OpenStreetMapLocation don't come with features
            // themes
            Theme theme = null; // Theme.valueOf(openStreetMapLocation.properties.get("theme").toString()) OpenStreetMapLocation don't come with themes

            Bar bar = new Bar(id, name, latitude, longitude, description, address, features, opening_hours, theme);
            jsonArray.add(gson.toJsonTree(bar).getAsJsonObject());
        }

        try (FileWriter fileWriter = new FileWriter("src" + outputJsonPath)) {
            gson.toJson(jsonArray, fileWriter);
        }
    }

    /**
     * Builds fast food location JSON for use in map. Pulls info from openStreetMap data that is configured to a JSON
     * for the application to use to get FastFood locations later when generating map. If the address isn't explicitly provided,
     * it is found using reverse geo-coding API from latitude longitude
     * @param outputJsonPath path from src e.g. "/main/resources/example.json"
     * @param openStreetMapFastFoodJsonPath path from resources e.g. "/json/example.json"
     */
    public void buildFastFoodFromInputJson(String outputJsonPath,
                                           String openStreetMapFastFoodJsonPath) throws IOException {
        JsonObject openStreetMapJsonObjectFastFood = (JsonObject) getJsonObject(openStreetMapFastFoodJsonPath);
        ArrayList<OpenStreetMapLocation> openStreetMapLocations = buildOpenStreetMapLocations(openStreetMapJsonObjectFastFood);

        JsonArray jsonArray = new JsonArray();

        for (OpenStreetMapLocation openStreetMapLocation : openStreetMapLocations) {
            // latitude and longitude
            ArrayList<Double> coords = getCoordsFromGeometryMap(openStreetMapLocation.geometry);
            double latitude = coords.get(1);
            double longitude = coords.get(0);
            // id
            String id = openStreetMapLocation.id;
            // name
            String name = (String) openStreetMapLocation.properties.get("name");
            // address
            String rawAddress = Objects.requireNonNullElseGet(
                    getAddressFromPropertiesMap(openStreetMapLocation.properties),
                    () -> APIService.getAddressFromCoords(latitude, longitude));
            String address =  rawAddress.startsWith(name + ", ") ? rawAddress.substring(name.length() + 2) : rawAddress;
            // opening times
            Map<DayOfWeek, ArrayList<LocalTime>> openingHours = getOpeningHoursFromPropertiesMap(openStreetMapLocation.properties);

            FastFood fastFood = new FastFood(id, name, address, latitude, longitude, openingHours);
            jsonArray.add(gson.toJsonTree(fastFood).getAsJsonObject());
        }
        try (FileWriter fileWriter = new FileWriter("src" + outputJsonPath)) {
            gson.toJson(jsonArray, fileWriter);
        }
    }

    /**
     * Builds features (drinking fountains/public toilets) JSON for use in map. Pulls info from OpenStreetMap data that
     * is configured to a JSON for the application to use to get simple locations such as drinking fountains and public
     * toilets later when generating map. If the address isn't explicitly provided, it is found using reverse geocoding
     * API from latitude longitude
     * @param outputJsonPath path from src e.g. "/main/resources/example.json"
     * @param CCCJsonPath path from resources package e.g. "/json/example.json"
     */
    public void buildLocationsJsonFromCCCLocationsJson(String outputJsonPath, String CCCJsonPath) throws IOException {
        JsonObject CCCJsonObject = (JsonObject) getJsonObject(CCCJsonPath);
        ArrayList<CCCLocation> cccLocations = buildCCCLocations(CCCJsonObject);

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < cccLocations.size(); i++) {

            CCCLocation cccLocation = cccLocations.get(i);

            ArrayList<Double> coords = getCoordsFromGeometryMap(cccLocation.geometry);
            double latitude = coords.get(1);
            double longitude = coords.get(0);

            Location location = new Location(
                    Integer.toString(i),
                    (String) cccLocation.getProperties().get("name"),
                    latitude,
                    longitude);

            jsonArray.add(gson.toJsonTree(location).getAsJsonObject());
        }

        try (FileWriter fileWriter = new FileWriter("src" + outputJsonPath)) {
            gson.toJson(jsonArray, fileWriter);
        }

    }

    /**
     * Finds latitude and longitude of geometry based on point, or first point, in geometry
     * @param geometry Map of coords, with "type" key showing type of geometry
     * @return 2-length ArrayList with [long, lat]
     */
    private ArrayList<Double> getCoordsFromGeometryMap(Map<String, Object> geometry) {
        ArrayList<Double> coords;
        if (geometry.get("type").equals("Point")) {
            coords = (ArrayList<Double>) geometry.get("coordinates");
        } else {
            coords = (ArrayList<Double>) (((ArrayList<ArrayList<Object>>) geometry.get("coordinates")).getFirst()).getFirst();
        }
        return coords;
    }

    /**
     * Finds address in properties map and returns String representation, or null if doesn't exist
     * @param properties Map of all properties of location
     * @return String representation of address, or null if does not exist
     */
    private String getAddressFromPropertiesMap(Map<String, Object> properties) {
        Object propertiesNumber = properties.get("addr:housenumber");
        Object propertiesStreet = properties.get("addr:street");
        Object propertiesSuburb = properties.get("addr:suburb");

        String number, street = null, suburb = null;

        if (propertiesNumber != null) {
            number = propertiesNumber + " ";
        } else {
            number = "";
        }

        if (propertiesStreet == null && propertiesSuburb == null) {
            return null;
        }
        if (propertiesStreet != null && propertiesSuburb != null) {
            street = number + propertiesStreet + ", ";
            suburb = propertiesSuburb.toString();
        } else if (propertiesStreet != null) {
            street = number + propertiesStreet;
            suburb = "";
        } else {
            street = "";
            suburb = propertiesSuburb.toString();
        }

        return street + suburb;
    }

    /**
     * Gets openingHours for use in Venue classes, helper method for "build" methods
     * @param properties Map generated from CCC/OpenStreetMap locations holding all informational data about Locations
     * @return Map from DayOfWeek enum to a two-long ArrayList with opening_time and closing_time for each day
     */
    private Map<DayOfWeek, ArrayList<LocalTime>> getOpeningHoursFromPropertiesMap(Map<String, Object> properties) {
        EnumMap<DayOfWeek, ArrayList<LocalTime>> openingHours = new EnumMap<>(DayOfWeek.class);

        if (properties.get("opening_hours") == null || !((String) properties.get("opening_hours")).matches(".*[0-9].*")) {
            return null;
        }
        String openingHoursString = (String) properties.get("opening_hours");
        String[] openingHoursDaysArray = openingHoursString.split("[;,] ");

        if (openingHoursDaysArray.length == 1 && !openingHoursDaysArray[0].matches(".*[a-zA-Z].*")) {
            ArrayList<LocalTime> hours = getHoursFromString(openingHoursDaysArray[0]);
            for (DayOfWeek day : DayOfWeek.values()) {
                openingHours.put(day, hours);
            }
        } else {
            for (String openingHoursDays : openingHoursDaysArray) {
                String[] dayAndHours = openingHoursDays.split(" ");
                String daysString;
                String hoursString;
                if (dayAndHours.length == 1) {
                    daysString = "Mo-Su";
                    hoursString = dayAndHours[0];
                } else if (dayAndHours.length >= 2) {
                    daysString = dayAndHours[0];
                    hoursString = dayAndHours[1];
                } else {
                    throw new IllegalArgumentException("Invalid day or hours format: " + Arrays.toString(dayAndHours));
                }

                ArrayList<LocalTime> hours = getHoursFromString(hoursString);

                for (DayOfWeek day : getDaysFromString(daysString)) {
                    openingHours.put(day, hours);
                }
            }
        }
        return openingHours;
    }

    /**
     * Translates a String into openingHours, if not a valid String, it will throw an IllegalArgumentException
     * @param hoursString String to be converted into openingHours
     * @return Two-length ArrayList with openingTime and closingTime
     */
    private ArrayList<LocalTime> getHoursFromString(String hoursString) {
        if (hoursString.matches("[0-2][0-9]:[0-5][0-9]\\+")) {
            hoursString = hoursString.replace("24:", "00:");
            LocalTime open = LocalTime.parse(hoursString.substring(0, hoursString.length() - 1));
            return new ArrayList<>(List.of(open, late));

        } else if (hoursString.matches("[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9]")) {
            hoursString = hoursString.replace("24:", "00:");
            String[] openAndClose = hoursString.split("-");
            LocalTime open = LocalTime.parse(openAndClose[0]);
            LocalTime close = LocalTime.parse(openAndClose[1]);
            return new ArrayList<>(List.of(open, close));

        } else if (hoursString.matches("[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9]\\+")) {
            hoursString = hoursString.replace("24:", "00:");
            String[] openAndClose = hoursString.split("-");
            LocalTime open = LocalTime.parse(openAndClose[0]);
            return new ArrayList<>(List.of(open, late));

        } else if (hoursString.matches("[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9],[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9]\\+?")) {
            return getHoursFromString(hoursString.substring(0, 6) + hoursString.substring(18));
        } else if (hoursString.matches("24/7")) {
            return getHoursFromString("00:00-00:00");
        } else if (hoursString.matches("off")) {
            return null;
        } else {
            throw new IllegalArgumentException("hoursString format error: " + hoursString);
        }
    }

    /**
     * Gets ArrayList of days from String. Used for determining openingHours of a Venue
     * @param daysString String to be converted to opening hours
     * @return Two-length ArrayList of DayOfWeek enums
     */
    private ArrayList<DayOfWeek> getDaysFromString(String daysString) {
        ArrayList<DayOfWeek> days = new ArrayList<>();
        if (daysString.length() == 2) {
            DayOfWeek day = getDayFromCode(daysString);
            if (day != null) {
                days.add(day);
            }
        } else if (daysString.contains(",")) {
            for (String dayStringTerm :  daysString.split(",")) {
                days.addAll(getDaysFromString(dayStringTerm));
            }
        } else if (daysString.contains("-")) {
            days.addAll(getDaysOfWeekFromHyphenPair(daysString));
        } else {
            throw new IllegalArgumentException("daysString format error: \"" + daysString + '"');
        }
        return days;
    }

    /**
     * Get days of week from String of hyphen pair e.g. Mo-Fr
     * @param daysString String to get days from
     * @return ArrayList of DayOfWeek enums
     */
    private ArrayList<DayOfWeek> getDaysOfWeekFromHyphenPair(String daysString) {
        String startDay = daysString.split("-")[0];
        String endDay = daysString.split("-")[1];
        DayOfWeek startDayOfWeek = getDayFromCode(startDay);
        DayOfWeek endDayOfWeek = getDayFromCode(endDay);

        ArrayList<DayOfWeek> days = new ArrayList<>();
        while (!startDayOfWeek.equals(endDayOfWeek)) {
            days.add(startDayOfWeek);
            startDayOfWeek = startDayOfWeek.plus(1);
        }
        days.add(startDayOfWeek);
        return days;
    }

    /**
     * Helper function to get day of the week from day code
     * @param dayCode String of code e.g. "Mo"
     * @return DayOfWeek enum, null if PH, throw error otherwise
     */
    private DayOfWeek getDayFromCode(String dayCode) {
        return switch (dayCode) {
            case "Mo" -> DayOfWeek.MONDAY;
            case "Tu" -> DayOfWeek.TUESDAY;
            case "We" -> DayOfWeek.WEDNESDAY;
            case "Th" -> DayOfWeek.THURSDAY;
            case "Fr" -> DayOfWeek.FRIDAY;
            case "Sa" -> DayOfWeek.SATURDAY;
            case "Su" -> DayOfWeek.SUNDAY;
            case "PH" -> null;
            case null, default -> throw new IllegalArgumentException("dayCode format error: " + dayCode);
        };
    }

}
