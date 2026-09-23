package seng202.team5.unittests.services;

import io.cucumber.java.bs.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.exceptions.NoStartBarException;
import seng202.team5.models.*;
import seng202.team5.services.LocationBuilder;
import seng202.team5.services.MainManager;
import seng202.team5.services.RouteService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RouteServiceTest {

    LocationBuilder builder = new LocationBuilder();
    ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);

    @Nested
    class testGeneratedRoute {

        MainManager mainManager;
        Bar startBar = allBars.get(57);
        Bar endBar = allBars.get(76);

        @BeforeEach
        public void setup() {
            mainManager = new MainManager();
        }

        @Test
        public void testedRouteReturnsValidParameters() {
            RouteService.generateRoute(mainManager, startBar, endBar, 3, 5.0, List.of(), List.of());

            assertNotNull(mainManager.currentRoute);
            assertEquals(startBar, mainManager.currentRoute.getStartBar());
            assertEquals(endBar, mainManager.currentRoute.getEndBar());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionDistance() {
            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () -> {
                RouteService.generateRoute(mainManager, startBar, endBar, 3, 1.0, List.of(), List.of());
            });
            assertEquals("Couldn't get close to your target distance of 1.0km.\nTry adjusting the filters.", thrown.getMessage());

        }

        @Test
        public void testThrowsNoStartBarExceptionWhenStartBarNull() {
            NoStartBarException thrown = assertThrows(NoStartBarException.class, () ->
                    RouteService.generateRoute(
                            mainManager, null, endBar, 3, 2.0, List.of(), List.of()));

            assertEquals("startBar has not been set", thrown.getMessage());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionThemes() {
            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () ->
                    RouteService.generateRoute(
                            mainManager, startBar, endBar, 3, 5.0, List.of(Theme.AIRPORT_LOUNGE, Theme.NEIGHBOURHOOD_PUB), List.of()));

            assertEquals("Couldn't fit your selected theme(s) into a route.\nTry adjusting the filters.", thrown.getMessage());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionFeatures() {
            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () ->
                    RouteService.generateRoute(
                            mainManager, startBar, endBar, 3, 5.0, List.of(), List.of(FeatureType.KARAOKE, FeatureType.GAMING_MACHINES)));

            assertEquals("Couldn't fit your selected feature(s) into a route.\nTry adjusting the filters.", thrown.getMessage());
        }
//        @Test
//        public void testedRouteThrowsNoRouteGeneratedExceptionParticulars() {
//            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () ->
//                    RouteService.generateRoute(
//                            mainManager, startBar, endBar, 3, 5.0, List.of(), List.of()));
//
//            assertEquals("Couldn't fit all your required bars into a route this size.\nTry adjusting the filters.", thrown.getMessage());
//        }

    }

    @Nested
    class testFindClosestFastFood {

        MainManager mainManager = new MainManager();
        Bar startBar = new Bar("bar0", "", 0, 0, "", "", null, null, null);
        Bar endBar = new Bar("bar0", "", 1, 1, "", "", null, null, null);
        List<Bar> bars;

        @BeforeEach
        public void setup() {
            bars = List.of(startBar, endBar);
        }

        @Test
        public void testZeroFastFood() {
            mainManager.locationManager.fastFoods = new ArrayList<>();
            assertEquals(0, RouteService.getClosestAmenities(1, mainManager.locationManager.fastFoods, bars
            ).size());
        }

        @Test
        public void testFastFoodClass() {
            mainManager.locationManager.fastFoods = new ArrayList<>(List.of(
                    new FastFood("node/0", "burger place", "1 Main Street", -1, -1, null)
            ));
            List<? extends Location> fastFoods = RouteService.getClosestAmenities(1, mainManager.locationManager.fastFoods, bars
            );
            assertEquals(1, fastFoods.size());
            assertEquals("node/0", fastFoods.getFirst().getLocID());
            assertTrue(fastFoods.stream().allMatch(fastFood -> fastFood.getClass().equals(FastFood.class)));
        }

        @Test
        public void testOneFastFood() {
            mainManager.locationManager.fastFoods = new ArrayList<>(List.of(
                    new FastFood("node/0", "burger place", "1 Main Street", -1, -1, null)
            ));
            List<? extends Location> fastFoods = RouteService.getClosestAmenities(1, mainManager.locationManager.fastFoods, bars
            );
            assertEquals(1, fastFoods.size());
            assertEquals("node/0", fastFoods.getFirst().getLocID());
        }
        @Test
        public void testTwoFastFood() {
            mainManager.locationManager.fastFoods = new ArrayList<>(List.of(
                    new FastFood("node/0", "burger place", "1 Main Street", 10, 10, null),
                    new FastFood("node/1", "burger place", "2 Main Street", 0.5, 0.5, null)
            ));
            assertEquals(1, RouteService.getClosestAmenities(2, mainManager.locationManager.fastFoods, bars
            ).size());
            assertEquals("node/1", RouteService.getClosestAmenities(2, mainManager.locationManager.fastFoods, bars
            ).getFirst().getLocID());
        }

        @Test
        public void testLessThanZeroNumLocations() {
            mainManager.locationManager.fastFoods = new ArrayList<>(List.of(
                    new FastFood("node/0", "burger place", "1 Main Street", 0, 0, null)
            ));
            assertThrows(IllegalArgumentException.class, () -> RouteService.getClosestAmenities(-1, mainManager.locationManager.fastFoods, bars
            ));
        }

        @Test
        public void testSizeFastFoodLessThanNumLocations() {
            mainManager.locationManager.fastFoods = new ArrayList<>(List.of(
                    new FastFood("node/0", "burger place", "1 Main Street", 0, 0, null)
            ));
            assertEquals(1, RouteService.getClosestAmenities(2, mainManager.locationManager.fastFoods, bars
            ).size());
        }

    }
}
