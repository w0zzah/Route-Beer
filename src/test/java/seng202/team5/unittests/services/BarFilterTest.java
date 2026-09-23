package seng202.team5.unittests.services;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seng202.team5.models.OpenStreetMapLocation;
import seng202.team5.services.BarFilter;
import seng202.team5.services.LocationBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BarFilterTest {

    LocationBuilder builder;
    ArrayList<OpenStreetMapLocation> allRestaurants;
    ArrayList<OpenStreetMapLocation> emptyRestaurants;
    ArrayList<OpenStreetMapLocation> noBarsRestaurants;

    @BeforeEach
    void setUp(){
        builder = new LocationBuilder();
        allRestaurants = builder.buildOpenStreetMapLocations(
                (JsonObject) builder.getJsonObject("/json/input/openStreetMapRestaurants.geojson")
        );
        noBarsRestaurants = builder.buildOpenStreetMapLocations(
                (JsonObject) builder.getJsonObject("/json/input/openStreetMapNoBarRestaurants.geojson")
        );
        emptyRestaurants = new ArrayList<>();
    }

    @Test
    public void testBarFilterEmpty() {
        ArrayList<OpenStreetMapLocation> filteredBars = BarFilter.filter(emptyRestaurants);

        assertEquals(0, filteredBars.size());
    }

    @Test
    public void testBarFilterNoBars() {
        ArrayList<OpenStreetMapLocation> filteredBars = BarFilter.filter(noBarsRestaurants);

        assertEquals(0, filteredBars.size());
    }

    @Test
    public void testBarFilterAllRestaurants() {
        ArrayList<OpenStreetMapLocation> filteredBars = BarFilter.filter(allRestaurants);

        assertTrue(0 < filteredBars.size());
        assertTrue(filteredBars.size() < allRestaurants.size());
    }
}