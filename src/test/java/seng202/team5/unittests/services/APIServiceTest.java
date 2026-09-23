package seng202.team5.unittests.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.models.Bar;
import seng202.team5.models.DistanceMatrix;
import seng202.team5.services.APIService;
import seng202.team5.services.LocationBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class APIServiceTest {

    LocationBuilder locationBuilder = new LocationBuilder();
    List<Bar> allBars = locationBuilder.buildLocations("/json/input/allAvailableBars.json", Bar.class);

    @Nested
    public class testGetAddress {
        @Test
        public void testGetAddressFromCoordsInvalid() {
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getAddressFromCoords(-91, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getAddressFromCoords(91, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getAddressFromCoords(0, -181);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getAddressFromCoords(0, 181);
            });
        }
        @Test
        public void testGetAddressFromCoordsAddressNotExist() {
            assertEquals("Earth", APIService.getAddressFromCoords(0, 0));
            // Null Island, so returns the least specific address; Earth
        }
        @Test
        public void testGetAddressFromCoordsAddressExist() {
            assertEquals(
                    "217 Manchester Street, Christchurch Central, Christchurch 8011, New Zealand",
                    APIService.getAddressFromCoords(-43.531282, 172.6394747));
        }
    }

    @Nested
    public class testDistanceMatrix {

        ArrayList<Bar> twoBars = locationBuilder.buildLocations("/json/input/twoBars.json", Bar.class);
        DistanceMatrix twoBarDistanceMatrix = new DistanceMatrix("/json/distanceMatrices/twoBarDistanceMatrix.json");

        @Test
        public void testGetDistanceMatrixNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getDistancesMatrix(null);
            });
        }

        @Test
        public void testGetDistanceMatrixEmpty() {
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getDistancesMatrix(new ArrayList<>());
            });
        }

        @Test
        public void testGetDistanceMatrixOneBar() {
            assertThrows(IllegalArgumentException.class, () -> {
                APIService.getDistancesMatrix(new ArrayList<>(
                        List.of(twoBars.getFirst()))
                );
            });
        }

        @Test
        public void testGetDistanceMatrixTwoBar() {
            DistanceMatrix testTwoBarDistanceMatrix = APIService.getDistancesMatrix(twoBars);
            assertEquals(twoBarDistanceMatrix.getSize(), testTwoBarDistanceMatrix.getSize());
            assertEquals(
                    twoBarDistanceMatrix.getDistanceKm(twoBars.getFirst(), twoBars.getLast()),
                    testTwoBarDistanceMatrix.getDistanceKm(twoBars.getFirst(), twoBars.getLast()));
        }
    }
}
