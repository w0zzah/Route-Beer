package seng202.team5.services;

import seng202.team5.models.*;

import java.util.*;
import java.util.Map;

/**
 * Class to handle the creation and storing of location objects. This acts as an MVC model, storing data
 * @author Neela van Gerwen
 */
public class LocationManager {

    public List<Bar> bars;
    public List<FastFood> fastFoods;
    public List<DrinkingFountain> drinkingFountains;
    public List<PublicToilet> publicToilets;
    private Map<String, Location> locationById;


    /**
     * Creates a new LocationManager object and calls the buildLocations method
     */
    public LocationManager() {
        buildLocations();
    }

    /**
     * Creates a new LocationBuilder object and uses it to convert location data from bars.json into Location objects
     */
    private void buildLocations() {
        LocationBuilder builder = new LocationBuilder();
        this.bars = builder.buildLocations("/json/data/bars.json", Bar.class);
        this.fastFoods = builder.buildLocations("/json/data/fast-food.json", FastFood.class);
        this.drinkingFountains = builder.buildLocations("/json/data/drinking-fountains.json", DrinkingFountain.class);
        this.publicToilets = builder.buildLocations("/json/data/public-toilets.json", PublicToilet.class);

        locationById = new HashMap<>();
        indexLocations(bars);
    }

    /**
     * Returns a random Location from the Locations list to the MainManager
     * @return a random Location object
     */
    Bar generateRandomBar() {
        Random rand = new Random();
        return this.bars.get(rand.nextInt(this.bars.size()));
    }

    public List<Bar> getBars() { return this.bars; }

    /**
     * Returns the names of all the bars managed by the current LocationManager
     * @return an ArrayList of names of the managed bars
     */
    public Collection<String> getBarNames() {
        Collection<String> barNames = new ArrayList<>(List.of());
        for (Bar bar : getBars()) {
            barNames.add(bar.getName());
        }
        return barNames;
    }

    /**
     * Adds the list of locations to the locationID lookup map
     * @param locations the list of locations to index
     */
    private void indexLocations(List<? extends  Location> locations) {
        for (Location location : locations) {
            locationById.put(location.getLocID(), location);
        }
    }

    public Location findById(String locID) {
        return locationById.get(locID);
    }

    /**
     * Finds bar with name matching the specified input name
     * ignores case and surrounding whitespace
     * @param name the name of a bar being queried
     * @return the matching Bar object, or null if no object is found
     */
    public Bar findBarByName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        for (Bar bar : getBars()) {
            if (bar.getName().equalsIgnoreCase(trimmed)) {
                return bar;
            }
        }
        return null;
    }

}
