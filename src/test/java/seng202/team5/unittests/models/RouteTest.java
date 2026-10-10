package seng202.team5.unittests.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.models.*;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.DistanceMatrix;
import seng202.team5.services.route_generation.LocationBuilder;
import seng202.team5.models.types.Theme;
import seng202.team5.models.types.FeatureType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    LocationBuilder builder = new LocationBuilder();
    ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);
    DistanceMatrix distanceMatrix = new DistanceMatrix("/json/distanceMatrices/barChoosingAlgorithmMatrix.json");

    List<Bar> bars = List.of(
            allBars.get(57), // The Craic
            allBars.get(75) // Fat Eddie's
    );

    @BeforeEach
    public void setup() {

    }
    @Nested
    public class testConstructor {
        @Test
        public void testMinimumConstructor() {
            Route route = new Route(0, "route_name", "route_description", bars, LocalDate.EPOCH, "Username");
            assertEquals(0, route.getRouteID());
            assertEquals("route_name", route.getName());
            assertEquals("route_description", route.getDescription());
            assertEquals(bars, route.getBars());
            assertEquals(3013, route.getTotalDistanceMetres());
            assertEquals(LocalDate.EPOCH, route.getCreatedDate());
        }
        @Test
        public void testConstructorWithNullName() {
            assertThrows(IllegalArgumentException.class, () -> {
                Route route = new Route(0, null, null, null, LocalDate.EPOCH, "Username");
            });
        }
        @Test
        public void testConstructorWithEmptyName() {
            assertThrows(IllegalArgumentException.class, () -> {
                Route route = new Route(0, "", null, null, LocalDate.EPOCH, "Username");
            });
        }
        @Test
        public void testConstructorWithNullBars() {
            assertThrows(IllegalArgumentException.class, () -> {
                Route route = new Route(0, "route_name", null, null, LocalDate.EPOCH, "Username");
            });
        }
        @Test
        public void testConstructorWithEmptyBars() {
            assertThrows(IllegalArgumentException.class, () -> {
                Route route = new Route(0, "route_name", null, List.of(), LocalDate.EPOCH, "Username");
            });
        }
        @Test
        public void testConstructorWithSingleBar() {
            assertThrows(IllegalArgumentException.class, () -> {
                Route route = new Route(0, "route_name", null, List.of(allBars.getFirst()), LocalDate.EPOCH, "Username");
            });
        }
        @Test
        public void testConstructorWithNullDescription() {
            Route route = new Route(0, "route_name", null, bars, LocalDate.EPOCH, "Username");
            assertEquals("", route.getDescription());
        }
//        @Test
//        public void testConstructorWithNullCreatedDate() {
//            Route route = new Route(0, "route_name", "description", bars, null);
//            assertEquals(LocalDate.now(), route.getCreatedDate());
//        }
    }

    @Nested
    class testDistinctGetters {

        Route route;
        @BeforeEach
        public void setup() {
            route = new Route(0, "route_name", "route_description", bars, LocalDate.EPOCH, "Username");
        }

        @Test
        public void testGetFeatureTypes() {
            assertTrue(route.getFeatureTypes().containsAll(List.of(
                    FeatureType.DART_BOARD,
                    FeatureType.KARAOKE,
                    FeatureType.POOL_TABLE,
                    FeatureType.LIVE_MUSIC,
                    FeatureType.OUTDOOR_SEATING
            )));
        }

        @Test
        public void testGetThemes() {
            assertTrue(route.getThemes().containsAll(List.of(
                    Theme.IRISH_PUB,
                    Theme.NEIGHBOURHOOD_PUB
            )));
        }
    }

}
