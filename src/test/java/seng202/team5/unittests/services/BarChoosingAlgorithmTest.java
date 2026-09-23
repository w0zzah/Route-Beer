package seng202.team5.unittests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.*;
import seng202.team5.services.BarChoosingAlgorithm;
import seng202.team5.services.LocationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BarChoosingAlgorithmTest {

    BarChoosingAlgorithm barChoosingAlgorithm;
    LocationBuilder builder = new LocationBuilder();

    ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);
    DistanceMatrix distanceMatrix = new DistanceMatrix("/json/distanceMatrices/barChoosingAlgorithmMatrix.json");
    Bar startBar;
    Bar endBar;
    double distance;
    Random seededRandom = new Random(0);

    @BeforeEach
    public void setup(){
        startBar = allBars.get(57); // The Craic
        endBar = allBars.get(76); // Fat Eddie's
        distance = 5.0;
    }

    @Test
    public void testBuildStartBarOnly(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertNotEquals(startBar, route.getEndBar());
        assertTrue(3 <= route.getNumStops() && route.getNumStops() <= 9);
        assertTrue(route.getTotalDistanceKm() <= 10);

    }
    @Test
    public void testBuildStartEnd(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertEquals(endBar, route.getEndBar());
        assertTrue(2 <= route.getNumStops() && route.getNumStops() <= 9);
        assertEquals(distanceMatrix.getDistanceKm(route.getStartBar(), route.getEndBar()), route.getTotalDistanceKm(), 1.5);
    }
    @Test
    public void testBuildStartDistance(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .approximateDistanceInKm(distance)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertNotEquals(startBar, route.getEndBar());
        assertTrue(3 <= route.getNumStops() && route.getNumStops() <= 9);
        assertEquals(5.0, route.getTotalDistanceKm(), 1.0);
    }
    @Test
    public void testBuildStartEndDistance(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .approximateDistanceInKm(distance)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertEquals(endBar, route.getEndBar());
        assertTrue(3 <= route.getNumStops() && route.getNumStops() <= 9);
        assertEquals(5.0, route.getTotalDistanceKm(), 1.0);
    }
    @Test
    public void testBuildStartNum(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .numberOfBars(5)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertNotEquals(startBar, route.getEndBar());
        assertEquals(5, route.getNumStops());
        assertTrue(route.getTotalDistanceKm() <= 10);
    }
    @Test
    public void testBuildStartEndNum(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .numberOfBars(5)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertEquals(endBar, route.getEndBar());
        assertEquals(5, route.getNumStops());
        assertEquals(distanceMatrix.getDistanceKm(route.getStartBar(), route.getEndBar()), route.getTotalDistanceKm(), 1.5);
    }
    @Test
    public void testBuildStartEndDistanceNum(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .approximateDistanceInKm(distance)
                .numberOfBars(5)
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertEquals(startBar, route.getStartBar());
        assertEquals(endBar, route.getEndBar());
        assertEquals(5, route.getNumStops());
        assertEquals(5.0, route.getTotalDistanceKm(), 1.0);
    }
    @Test
    public void testBuildStartEndDistanceNumTooStrict(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .approximateDistanceInKm(0.5)
                .numberOfBars(5)
                .build(seededRandom);
        assertThrows(NoRouteGeneratedException.class, () -> barChoosingAlgorithm.getRoute());
    }
    @Test
    public void testBuildFeaturesNull(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .features(null)
                .build(seededRandom);
        assertDoesNotThrow(() -> barChoosingAlgorithm.getRoute());
    }
    @Test
    public void testBuildFeaturesSingle(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .features(List.of(
                        FeatureType.KARAOKE)
                )
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertTrue(route.getFeatureTypes().contains(FeatureType.KARAOKE));
    }
    @Test
    public void testBuildThemesNull(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .themes(null)
                .build(seededRandom);
        assertDoesNotThrow(() -> barChoosingAlgorithm.getRoute());
    }
    @Test
    public void testBuildThemesSingle(){
        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .themes(List.of(
                        Theme.IRISH_PUB
                ))
                .build(seededRandom);
        Route route = barChoosingAlgorithm.getRoute();
        assertTrue(route.getThemes().contains(Theme.IRISH_PUB));
    }

    @Nested
    public class testDiagnose {

        BarChoosingAlgorithm.Builder barChoosingAlgBuilder;

        @BeforeEach
        public void setup(){
            barChoosingAlgBuilder = new BarChoosingAlgorithm.Builder(startBar, allBars)
                    .endBar(endBar)
                    .numberOfBars(3);
        }

        @Test
        public void testDiagnoseParticularBars() throws IOException {
            barChoosingAlgorithm = barChoosingAlgBuilder
                    .particularBars(allBars.subList(0, 2))
                    .build();

            NoRouteGeneratedException exception = assertThrows(
                    NoRouteGeneratedException.class,
                    () -> barChoosingAlgorithm.getRoute()
            );
            assertEquals(
                    "Couldn't fit all your required bars into a route this size.\nTry adjusting the filters.",
                    exception.getMessage()
            );
        }
        @Test
        public void testDiagnoseThemes() throws IOException {
            barChoosingAlgorithm = barChoosingAlgBuilder
                    .themes(List.of(
                            Theme.AIRPORT_LOUNGE
                    ))
                    .build();

            NoRouteGeneratedException exception = assertThrows(
                    NoRouteGeneratedException.class,
                    () -> barChoosingAlgorithm.getRoute()
            );
            assertEquals(
                    "Couldn't fit your selected theme(s) into a route.\nTry adjusting the filters.",
                    exception.getMessage()
            );
        }
        @Test
        public void testDiagnoseFeatures() throws IOException {
            barChoosingAlgorithm = barChoosingAlgBuilder
                    .approximateDistanceInKm(5.0) // don't let bar options wander too far by leaving unset
                    .features(List.of(FeatureType.values()))
                    .build();

            NoRouteGeneratedException exception = assertThrows(
                    NoRouteGeneratedException.class,
                    () -> barChoosingAlgorithm.getRoute()
            );
            assertEquals(
                    "Couldn't fit your selected feature(s) into a route.\nTry adjusting the filters.",
                    exception.getMessage()
            );
        }
        @Test
        public void testDiagnoseApproxDistance() throws IOException {
            barChoosingAlgorithm = barChoosingAlgBuilder
                    .approximateDistanceInKm(0.1)
                    .build();

            NoRouteGeneratedException exception = assertThrows(
                    NoRouteGeneratedException.class,
                    () -> barChoosingAlgorithm.getRoute()
            );
            assertEquals(
                    "Couldn't get close to your target distance of 0.1km.\nTry adjusting the filters.",
                    exception.getMessage()
            );
        }
    }
//    particular bars will be tested in deliverable 3
//    @Test
//    public void testBuildParticularBarsNull(){
//        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, bars)
//                .endBar(endBar)
//                .particularBars(null)
//                .build(seededRandom);
//        assertDoesNotThrow(() -> barChoosingAlgorithm.getRoute());
//    }
//    @Test
//    public void testBuildParticularBarsSingle(){
//        barChoosingAlgorithm = new BarChoosingAlgorithm.Builder(startBar, bars)
//                .endBar(endBar)
//                .particularBars(List.of(
//                        bars.getFirst() // Bush Inn
//                ))
//                .build(seededRandom);
//        Route route = barChoosingAlgorithm.getRoute();
//        assertTrue(route.getBars().contains(bars.getFirst()));
//    }
}
