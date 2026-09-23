package seng202.team5.unittests.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import seng202.team5.models.Bar;
import seng202.team5.models.Location;

public class LocationTest {

    Bar bar;
    Location location;

    @BeforeEach
    public void setUp() {
        bar = new Bar("0", "name", 0.0, 0.0, "", "", null, null, null);
        location = new Location("1", "name", 1.0, 1.0);
    }

    @Test
    public void testGeoapifyFormat() {
        assertEquals("{\"location\": [0.0000000, 0.0000000]}", bar.getLocationForGeoapify());
        assertEquals("{\"location\": [1.0000000, 1.0000000]}", location.getLocationForGeoapify());
    }

}
