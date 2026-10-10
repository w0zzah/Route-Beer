package seng202.team5.unittests.models;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import seng202.team5.models.locations.Bar;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BarTest {

    Bar bar;

    @Nested
    public class testPrint {

        @Test
        public void testPrintHoursNoHours() {
            bar = new Bar("node/1", "bar1", 0, 0, "desc", "1 Road", null, new HashMap<>(), null);
            assertEquals("Hours unavailable", bar.printHours());
        }

        @Test
        public void testPrintHoursEmptyMap() {
            Bar emptyHoursBar = new Bar("node/1", "bar1", 0, 0, "desc", "1 Road", null, new HashMap<>(), null);
            assertEquals("Hours unavailable", emptyHoursBar.printHours());
        }

        @Test
        public void testPrintHoursNormal() {
            Map<DayOfWeek, ArrayList<LocalTime>> openingHours = new HashMap<>();
            ArrayList<LocalTime> mondayTimes = new ArrayList<>();
            mondayTimes.add(LocalTime.of(9, 0));
            mondayTimes.add(LocalTime.of(17, 0));
            openingHours.put(DayOfWeek.MONDAY, mondayTimes);

            Bar hoursBar = new Bar("node/2", "bar2", 0, 0, "desc", "1 Road", null, openingHours, null);
            String result = hoursBar.printHours();
            // assertTrue(result.contains("Mon, 9:00 am - 5:00 pm"));
        }

        @Test
        public void testPrintHoursLate() {
            Map<DayOfWeek, ArrayList<LocalTime>> openingHours = new HashMap<>();
            ArrayList<LocalTime> fridayTimes = new ArrayList<>();
            fridayTimes.add(LocalTime.of(20, 0));
            fridayTimes.add(LocalTime.of(23, 59));
            openingHours.put(DayOfWeek.FRIDAY, fridayTimes);

            Bar hoursBar = new Bar("node/3", "bar3", 0, 0, "desc", "1 Road", null, openingHours, null);
            String result = hoursBar.printHours();
            // assertTrue(result.contains("Fri, 8:00 pm - Late"));
        }

        @Test
        public void testPrintHoursClosedDay() {
            Map<DayOfWeek, ArrayList<LocalTime>> openingHours = new HashMap<>();
            ArrayList<LocalTime> mondayTimes = new ArrayList<>();
            mondayTimes.add(LocalTime.of(9, 0));
            mondayTimes.add(LocalTime.of(17, 0));
            openingHours.put(DayOfWeek.MONDAY, mondayTimes);

            Bar hoursBar = new Bar("node/4", "bar4", 0, 0, "desc", "1 Road", null, openingHours, null);
            String result = hoursBar.printHours();
            assertTrue(result.contains("Tue, Closed"));
        }

        @Test
        public void testPrintFeaturesEmpty() {
            Bar noFeaturesBar = new Bar("node/5", "bar5", 0, 0, "desc", "1 Road", new ArrayList<>(), null, null);
            assertNull(noFeaturesBar.printFeatures());
        }

    }

}