package seng202.team5.services;

import seng202.team5.exceptions.InvalidParametersException;
import seng202.team5.exceptions.InvalidParametersReason;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.Route;
import seng202.team5.models.RouteFormat;
import seng202.team5.models.locations.Bar;

import java.util.Collection;

/**
 * Business logic behind the route builder: validates a {@link RouteFormat},
 * resolves bars, and asks {@link RouteService} to generate routes.
 * Contains no JavaFX types so it can be unit tested directly.
 */
public class RouteBuilderService {

    /** Fewest bars a generated route may contain. */
    public static final int MIN_BARS = 2;
    /** Most bars a generated route may contain. */
    public static final int MAX_BARS = 9;
    /** How many candidate routes to generate per request. */
    public static final int ROUTES_TO_GENERATE = 1;

    private final MainManager mainManager;

    /**
     * @param mainManager the application's main manager
     */
    public RouteBuilderService(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    /**
     * Validates the request, generates routes and returns the best one.
     *
     * @param request the user's inputs
     * @return the first generated route
     * @throws InvalidParametersException if the start/end bar is missing or unknown
     * @throws NoRouteGeneratedException if no route could be built
     */
    public Route generateRoute(RouteFormat request)
            throws InvalidParametersException, NoRouteGeneratedException {
        Bar startBar = resolveStartBar(request.startBarInput());
        Bar endBar = resolveEndBar(request.endBarInput());
        Double approxDistanceKm = request.approxDistanceInKmTextInput();

        RouteService.generateRoutes(mainManager, request, ROUTES_TO_GENERATE);

        return mainManager.getGeneratedRoute(0);
    }

    /**
     * @param name bar name (exact match)
     * @return the bar, or null if none matches
     */
    public Bar findBar(String name) {
        return mainManager.locationManager.findBarByName(name);
    }

    /**
     * @return all bar names, for autocompletion
     */
    public Collection<String> getBarNames() {
        return mainManager.locationManager.getBarNames();
    }

    /**
     * @return the route currently being displayed, or null
     */
    public Route getCurrentRoute() {
        return mainManager.getCurrentRoute();
    }

    /**
     * @param route the route now being displayed
     */
    public void setCurrentRoute(Route route) {
        mainManager.setCurrentRoute(route);
    }

    /**
     * Checks the inputted startBar parameter when generating a route
     * @param name name of the bar being queried
     * @return the corresponding Bar type of the inputted bar name
     * @throws InvalidParametersException if the user has inputted invalid parameters
     */
    private Bar resolveStartBar(String name) throws InvalidParametersException {
        if (name == null || name.isBlank()) {
            throw new InvalidParametersException(InvalidParametersReason.START_BAR_EMPTY);
        }
        Bar bar = findBar(name);
        if (bar == null) {
            throw new InvalidParametersException(InvalidParametersReason.START_BAR_DOESNT_EXIST);
        }
        return bar;
    }

    /**
     * Checks the inputted endBar parameter when generating a route
     * @param name name of the bar being queried, or null if left blank
     * @return the corresponding Bar type of the inputted bar name or null if left blank
     * @throws InvalidParametersException if the user has inputted invalid parameters
     */
    private Bar resolveEndBar(String name) throws InvalidParametersException {
        if (name == null || name.isBlank()) {
            return null; // end bar is optional
        }
        Bar bar = findBar(name);
        if (bar == null) {
            throw new InvalidParametersException(InvalidParametersReason.END_BAR_DOESNT_EXIST);
        }
        return bar;
    }

    /**
     * @return the distance in km, or null if blank or not a number (e.g. just ".")
     */
    public Double parseDistance(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}