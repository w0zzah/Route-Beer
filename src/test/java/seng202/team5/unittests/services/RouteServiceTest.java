package seng202.team5.unittests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.exceptions.InvalidParametersException;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.RouteFormat;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.FastFood;
import seng202.team5.models.locations.Location;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;
import seng202.team5.services.route_generation.LocationBuilder;
import seng202.team5.services.MainManager;
import seng202.team5.services.RouteService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seng202.team5.exceptions.InvalidParametersReason.*;

public class RouteServiceTest {

    LocationBuilder builder = new LocationBuilder();
    ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);

    @Nested
    class testGeneratedRoute {

        MainManager mainManager;
        String startBarInput = "The Craic";
        Bar startBar = allBars.get(57);
        String endBarInput = "Fat Eddie's";
        Bar endBar = allBars.get(75);

        List<Bar> threeParticularBars = List.of(
                allBars.get(1), allBars.get(2), allBars.get(3)
        );

        @BeforeEach
        public void setup() {
            mainManager = new MainManager();
        }

//        @Test
//        public void testedRouteReturnsValidParameters() {
//            RouteService.generateRoute(mainManager, startBar, endBar, 3, 5.0, List.of(), List.of(), List.of());
//
//            assertNotNull(mainManager.currentRoute);
//            assertEquals(startBar, mainManager.currentRoute.getStartBar());
//            assertEquals(endBar, mainManager.currentRoute.getEndBar());
//        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionDistance() {
            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () -> {
                RouteFormat RouteFormat = new RouteFormat(startBarInput, endBarInput, 3, 1.0, List.of(), List.of(), List.of());
                RouteService.generateRoute(mainManager, RouteFormat);
            });
            assertEquals("Couldn't get close to your target distance of 1.0km.\nTry adjusting the filters.", thrown.getMessage());

        }

        @Test
        public void testThrowsInvalidParameterExceptionWhenStartBarNull() {
            InvalidParametersException thrown = assertThrows(InvalidParametersException.class, () -> {
                RouteFormat RouteFormat = new RouteFormat("", "", 3, 1.0, List.of(), List.of(), List.of());
                RouteService.generateRoute(mainManager, RouteFormat);
            });

            assertEquals(START_BAR_EMPTY, thrown.getReason());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionThemes() {
            InvalidParametersException thrown = assertThrows(InvalidParametersException.class, () -> {
                RouteFormat RouteFormat = new RouteFormat(startBarInput, endBarInput, 2, 5.0, List.of(Theme.IRISH_PUB, Theme.NEIGHBOURHOOD_PUB, Theme.NIGHTCLUB), List.of(), List.of());
                RouteService.generateRoute(mainManager, RouteFormat);
            });

            assertEquals(TOO_MANY_THEMES, thrown.getReason());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionFeatures() {
            NoRouteGeneratedException thrown = assertThrows(NoRouteGeneratedException.class, () -> {
                RouteFormat RouteFormat = new RouteFormat(startBarInput, endBarInput, 2, 5.0, List.of(), List.of(FeatureType.KARAOKE, FeatureType.GAMING_MACHINES, FeatureType.DART_BOARD, FeatureType.LIVE_MUSIC), List.of());
                RouteService.generateRoute(mainManager, RouteFormat);
            });

            assertEquals("Couldn't fit your selected feature(s) into a route.\nTry adjusting the filters.", thrown.getMessage());
        }

        @Test
        public void testedRouteThrowsNoRouteGeneratedExceptionParticulars() {
            InvalidParametersException thrown = assertThrows(InvalidParametersException.class, () -> {
                RouteFormat RouteFormat = new RouteFormat(startBarInput, endBarInput, 2, 5.0, List.of(), List.of(), threeParticularBars);
                RouteService.generateRoute(mainManager, RouteFormat);
            });

            assertEquals(TOO_MANY_PARTICULAR_BARS, thrown.getReason());
        }

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
