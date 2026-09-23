package seng202.team5.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.*;
import seng202.team5.services.BarChoosingAlgorithm;
import seng202.team5.services.LocationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateRouteSteps {

    private final LocationBuilder builder = new LocationBuilder();
    private final ArrayList<Bar> allBars = builder.buildLocations("/json/input/allAvailableBars.json", Bar.class);
    private final DistanceMatrix distanceMatrix = new DistanceMatrix("/json/distanceMatrices/barChoosingAlgorithmMatrix.json");

    private Bar startBar;
    private Bar endBar;
    private final Random seededRandom = new Random(0);

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
        BarChoosingAlgorithm algorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .build(seededRandom);
        generatedRoute = algorithm.getRoute();
    }

    @Then("A random number of bars between 3-9 are shown on the map interconnected with total walking distance")
    public void verifyNumberOfBarsAndConnections() {
        int numStops = generatedRoute.getNumStops();
        assertTrue(numStops > 15, "Route should have between 3 and 9 stops.");
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
        BarChoosingAlgorithm algorithm = new BarChoosingAlgorithm.Builder(startBar, allBars)
                .endBar(endBar)
                .approximateDistanceInKm(5.0)
                .numberOfBars(5)
                .features(List.of(FeatureType.KARAOKE))
                .themes(List.of(Theme.IRISH_PUB))
                .build(seededRandom);
        generatedRoute = algorithm.getRoute();
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
                    algBuilder.approximateDistanceInKm(5.0)
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Then("a NoRouteGeneratedException is thrown with the message {string}")
    public void verifyExceptionAndMessage(String expectedMessage) {
        assertNotNull(caughtException, "Expected a NoRouteGeneratedException to be thrown.");
        assertInstanceOf(NoRouteGeneratedException.class, caughtException);

        // Handle newline character escaping from Gherkin feature file
        String formattedExpectedMessage = expectedMessage.replace("\\n", "\n");
        assertEquals(formattedExpectedMessage, caughtException.getMessage());
    }
}