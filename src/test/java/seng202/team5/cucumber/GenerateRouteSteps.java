package seng202.team5.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.Route;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;
import seng202.team5.services.route_generation.BarChoosingAlgorithm;
import seng202.team5.services.route_generation.LocationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateRouteSteps {
    private final LocationBuilder builder = new LocationBuilder();
    private final ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);

    private Bar startBar;
    private Bar endBar;
    private final Random seededRandom = new Random(3);

    private Route generatedRoute;
    private Exception caughtException;
    private BarChoosingAlgorithm.Builder algBuilder;

    @Given("The user is on the main screen")
    public void theUserIsOnTheMainScreen() {
        startBar = allBars.get(57); // The Craic
        endBar = allBars.get(76); // Fat Eddie's
        caughtException = null;
        generatedRoute = null;
    }

    @When("The user selects generate route with a valid starting location")
    public void theUserSelectsGenerateRouteWithAValidStartingLocation() {
        long endTime = System.currentTimeMillis() + 4000;
        while (generatedRoute == null) {
            try {
                generatedRoute = new BarChoosingAlgorithm.Builder(startBar, allBars)
                        .build(seededRandom)
                        .getRoute();
            } catch (NoRouteGeneratedException e) {
                if (System.currentTimeMillis() > endTime) {
                    throw e;
                }
            }
        }
    }

    @Then("A random number of bars between 3-9 are shown on the map interconnected with total walking distance")
    public void verifyNumberOfBarsAndConnections() {
        int numStops = generatedRoute.getNumStops();
        assertTrue(numStops >= 3 && numStops <= 9, "Route should have between 3 and 9 stops.");
        assertTrue(generatedRoute.getTotalDistanceKm() > 0, "Route should be longer than 0km");
    }

    @Then("the route starts at the chosen starting location")
    public void verifyStartBar() {
        assertEquals(startBar, generatedRoute.getStartBar());
        assertNotEquals(startBar, generatedRoute.getEndBar());
    }

    @Then("the route total distance is under 10 km")
    public void verifyMaxDistance() {
        assertTrue(generatedRoute.getTotalDistanceKm() <= 10.0);
    }

    @When("the user selects Create Route with all the available fields filled out \\(start, end, distance, number of bars, themes, and features)")
    public void createRouteWithAllFilters() {
        long endTime = System.currentTimeMillis() + 4000;
        while (generatedRoute == null) {
            try {
                generatedRoute = new BarChoosingAlgorithm.Builder(startBar, allBars)
                        .endBar(endBar)
                        .approximateDistanceInKm(6.0)
                        .numberOfBars(5)
                        .features(List.of(FeatureType.KARAOKE))
                        .themes(List.of(Theme.IRISH_PUB))
                        .build(seededRandom)
                        .getRoute();
            } catch (NoRouteGeneratedException e) {
                if (System.currentTimeMillis() > endTime) {
                    throw e;
                }
            }
        }
    }

    @Then("a route is generated matching the specified start and end bars")
    public void verifyStartAndEndBars() {
        assertEquals(startBar, generatedRoute.getStartBar());
        assertEquals(endBar, generatedRoute.getEndBar());
    }

    @Then("the route has exactly {int} bars")
    public void verifyExactNumberOfBars(int expectedBars) {
        assertEquals(expectedBars, generatedRoute.getNumStops());
    }

    @Then("the route's total distance is approximately {double} km")
    public void verifyApproximateDistance(double expectedDistance) {
        assertEquals(expectedDistance, generatedRoute.getTotalDistanceKm(), 1.0);
    }

    @Then("the route includes the requested features and themes")
    public void verifyFeaturesAndThemes() {
        assertTrue(generatedRoute.getFeatureTypes().contains(FeatureType.KARAOKE));
        assertTrue(generatedRoute.getThemes().contains(Theme.IRISH_PUB));
    }

    @When("the user applies a strict {word} filter that conflicts with the constraints")
    public void applyStrictFilters(String filterType) {
        algBuilder = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .numberOfBars(3);

        try {
            switch (filterType) {
                case "particular_bars":
                    algBuilder.particularBars(allBars.subList(0, 2)).build().getRoute();
                    break;
                case "themes":
                    algBuilder.themes(List.of(Theme.AIRPORT_LOUNGE)).build().getRoute();
                    break;
                case "features":
                    algBuilder.approximateDistanceInKm(6.0)
                            .features(List.of(FeatureType.values()))
                            .build().getRoute();
                    break;
                case "approx_distance":
                    algBuilder.approximateDistanceInKm(0.1).build().getRoute();
                    break;
                default:
                    fail("Unknown filter type: " + filterType);
            }
        } catch (NoRouteGeneratedException e) {
            caughtException = e;
        }
    }

    @Then("a NoRouteGeneratedException is thrown with the message {string}")
    public void verifyExceptionAndMessage(String expectedMessage) {
        assertNotNull(caughtException, "Expected a NoRouteGeneratedException to be thrown.");
        String formattedExpectedMessage = expectedMessage.replace("\\n", "\n");
        assertEquals(formattedExpectedMessage, caughtException.getMessage());
    }

}