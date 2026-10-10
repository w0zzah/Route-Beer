package seng202.team5.services.route_generation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.models.*;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.locations.DistanceMatrix;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;

import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Contains bar algorithms for determining bars for route. Uses builder pattern to construct end user set
 * parameters. Doesn't solve the traveling salesman problem (TSP), just finds an ordered list of bars for the
 * route to follow
 * @author tba132
 */
public class BarChoosingAlgorithm {

    private final Bar mainStartBar; // compulsory
    private final Bar mainEndBar;
    private final int approximateTotalDistanceInMetres;
    private final int numberOfBars;
    private final List<Bar> particularBars;
    private final List<Theme> themes;
    private final List<FeatureType> features;
    
    public static final DistanceMatrix distanceMatrix = new DistanceMatrix("/json/data/distance-matrix.json");

    private final Random random;

    // These values are all magic numbers that can be changed to make the algorithm change
    // CHANGING THESE MAY MAKE TESTS FAIL
    private final int NUMBER_OF_CANDIDATE_BARS = 5;
    private final int NUMBER_OF_CANDIDATE_ROUTES = 500;
    private final int NUMBER_OF_FINAL_CANDIDATE_ROUTES = 10;

    private static final int NUM_ERROR_COMPONENTS_BAR = 2;
    private final double MAIN_DISTANCE_WEIGHT_BAR = 1;
    private final double OTHERS_DISTANCES_WEIGHT_BAR = 0.4;

    private static final int NUM_ERROR_COMPONENTS_ROUTES = 2;
    private final double MAIN_DISTANCE_WEIGHT_ROUTE = 0.5;
    private final double SPACING_WEIGHT_ROUTE = 0.5;

    private final double DISTANCE_ERROR = 0.2;

    private final List<Bar> availableBars;

    private final Logger logger = LogManager.getLogger(BarChoosingAlgorithm.class);

    /**
     * Constructs a new BarChoosingAlgorithm using the parameters specified
     * @param builder the builder containing the route parameters
     * @throws IOException if the distance matrix cannot be loaded
     */
    private BarChoosingAlgorithm(Builder builder, Random random) {
        this.mainStartBar = builder.startBar;
        this.mainEndBar = builder.endBar;
        this.approximateTotalDistanceInMetres = (int) Math.floor(builder.approximateDistanceInKm * 1000);
        this.numberOfBars = builder.numberOfBars;
        this.particularBars = builder.particularBars;
        this.themes = builder.themes;
        this.features = builder.features;
        this.availableBars = builder.availableBars;
        this.random = random;
    }

    /**
     * Builder pattern for creating algorithm class
     */
    public static class Builder {
        private final Bar startBar; // compulsory
        private final List<Bar> availableBars; // compulsory - pool the algorithm may choose from
        private Bar endBar;
        private static final long NUMBER_OF_CANDIDATE_END_BARS = 5;
        private Double approximateDistanceInKm;
        private final double MAXIMUM_DISTANCE_IF_NULL = 10.0; // 10.0km
        private int numberOfBars;
        private List<Bar> particularBars;
        private List<Theme> themes;
        private List<FeatureType> features;

        /**
         * Used to construct user selected parameters to generate a route
         * @param availableBars an arraylist of all bars in dataset, from MainManager
         */
        public Builder(Bar startBar, List<Bar> availableBars) {
            this.startBar = startBar;
            this.availableBars = availableBars;
        }

        public Builder endBar(Bar endBar) {
            this.endBar = endBar;
            return this;
        }

        public Builder approximateDistanceInKm(Double approximateDistanceInKm) {
            this.approximateDistanceInKm = approximateDistanceInKm;
            return this;
        }

        public Builder numberOfBars(int numberOfBars) {
            this.numberOfBars = numberOfBars;
            return this;
        }

        public Builder particularBars(List<Bar> particularBars) {
            this.particularBars = particularBars;
            return this;
        }

        public Builder themes(List<Theme> themes) {
            this.themes = themes;
            return this;
        }

        public Builder features(List<FeatureType> features) {
            this.features = features;
            return this;
        }

        public BarChoosingAlgorithm build() {
            return build(new Random());
        }

        public BarChoosingAlgorithm build(Random random) {
            if (this.numberOfBars < 2) {
                this.numberOfBars = random.nextInt(3, 10);
            }
            if (this.particularBars == null) {
                this.particularBars = new ArrayList<>();
            }
            if (this.endBar == null) {
                List<Bar> candidateBars;
                if (particularBars.size() + 1 == numberOfBars) {
                    candidateBars = particularBars;
                } else {
                    candidateBars = getCandidateEndBars();
                }
                this.endBar = candidateBars.get(random.nextInt(candidateBars.size()));
            }
            if (this.approximateDistanceInKm == null) {
                double[] distanceStartToEnd = {Double.MAX_VALUE};
                getMinDistanceInKm(0, distanceStartToEnd);
                double minDistanceInKm = distanceStartToEnd[0];
                if (minDistanceInKm == 0) {
                    this.approximateDistanceInKm = MAXIMUM_DISTANCE_IF_NULL / 1.5;
                } else {
                    this.approximateDistanceInKm = random.nextDouble(distanceStartToEnd[0], distanceStartToEnd[0] * 1.5);
                }
            }
            if (this.themes == null) {
                this.themes = new ArrayList<>();
            }
            if (this.features == null) {
                this.features = new ArrayList<>();
            }
            return new BarChoosingAlgorithm(this, random);
        }

        private @NonNull List<Bar> getCandidateEndBars() {
            Stream<Bar> candidateBarStream = availableBars.stream()
                    .filter(bar -> !bar.equals(startBar));
            if (this.approximateDistanceInKm != null) {
                candidateBarStream = candidateBarStream
                    .sorted(Comparator.comparing(bar ->
                            Math.abs(distanceMatrix.getDistanceKm(bar, startBar) - approximateDistanceInKm)))
                    .limit(NUMBER_OF_CANDIDATE_END_BARS);
            } else {
                candidateBarStream = candidateBarStream
                        .filter(bar -> distanceMatrix.getDistanceKm(bar, startBar) <= MAXIMUM_DISTANCE_IF_NULL);
            }
            return candidateBarStream.toList();
        }

        /**
         * Calculates minimum distance of route from start to end considering particular bars to be included, recursive brute-force TSP to insert particular bars
         */
        private void getMinDistanceInKm(int index, double[] minDistanceKm) {
            if (particularBars.isEmpty()) {
                minDistanceKm[0] = distanceMatrix.getDistanceKm(startBar, endBar);
                return;
            }
            if (index >= particularBars.size()) {
                double currentDistanceKm = 0.0;
                Bar currentBar = startBar;

                for (Bar nextBar : particularBars) {
                    currentDistanceKm += distanceMatrix.getDistanceKm(currentBar, nextBar);
                    currentBar = nextBar;
                }
                currentDistanceKm += distanceMatrix.getDistanceKm(currentBar, endBar);

                if (currentDistanceKm < minDistanceKm[0]) {
                    minDistanceKm[0] = currentDistanceKm;
                }
                return;
            }

            for (int i = index; i < particularBars.size(); i++) {
                Collections.swap(particularBars, index, i);
                getMinDistanceInKm(index + 1, minDistanceKm);
                Collections.swap(particularBars, index, i);
            }
        }
    }

    /**
     * <b>Algorithm option recursion</b>
     * Find midpoint bar M between start and end
     * Find midpoint bar between start and M; then M and end
     * Repeat recursively
     * @return ArrayList of chosen bars
     */
    public ArrayList<Bar> getChosenBarsRecursively() {
        if (numberOfBars < 2) {
            return new ArrayList<>(List.of(mainStartBar, mainEndBar));
        }

        // fullRoute is the single source of truth for "what's been chosen and where".
        // barSegment.subList(...) views passed through the recursion are backed by
        // this same array, so any .set() on a sub-view is automatically visible
        // here too - no separate index-tracking structure needed.
        ArrayList<Bar> fullRoute = new ArrayList<>(Collections.nCopies(numberOfBars, null));
        fullRoute.set(0, mainStartBar);
        fullRoute.set(numberOfBars - 1, mainEndBar);

        for (Bar particularBar : particularBars) {
            int index = getIndexForParticularBar(particularBar);
            if (fullRoute.get(index) == null) {
                fullRoute.set(index, particularBar);
            }
        }

        return new ArrayList<>(getChosenBarRecursionDP(fullRoute, fullRoute, 0));
    }

    /**
     * Recursively selects midpoint bars for a segment of the route
     * @param barSegment the section of the route currently being filled (a view over fullRoute)
     * @param fullRoute the complete route(int) random.nextGaussian(idealIndex, (double) 1/3) array; null where a bar hasn't been chosen yet
     * @param segmentStartIndexInRoute the global route index that barSegment.getFirst() corresponds to
     * @return the ordered list of bars for the current segment
     */
    private List<Bar> getChosenBarRecursionDP(List<Bar> barSegment, List<Bar> fullRoute,
                                              int segmentStartIndexInRoute) {
        if (barSegment.size() == 2) {
            return barSegment;
        }

        int midpoint = barSegment.size() / 2;
        int candidateRouteIndex = segmentStartIndexInRoute + midpoint;

        List<Bar> allBarsNotInRoute = availableBars.stream()
                .filter(bar -> !fullRoute.contains(bar))
                .toList();

        Bar chosenMidPointBar = chooseCandidateBar(allBarsNotInRoute, fullRoute, candidateRouteIndex);

        barSegment.set(midpoint, chosenMidPointBar); // propagates into fullRoute via the live subList view

        // recursion is here
        List<Bar> firstHalf = getChosenBarRecursionDP(barSegment.subList(0, midpoint + 1),
                fullRoute, segmentStartIndexInRoute);
        List<Bar> secondHalf = getChosenBarRecursionDP(barSegment.subList(midpoint, barSegment.size()),
                fullRoute, segmentStartIndexInRoute + midpoint);

        ArrayList<Bar> outputBars = new ArrayList<>(firstHalf);
        outputBars.addAll(secondHalf.subList(1, secondHalf.size()));
        return outputBars;
    }

    private int getIndexForParticularBar(Bar bar) {
        int distanceFromStart = distanceMatrix.getDistanceMetres(mainStartBar, bar);
        int distanceFromEnd = distanceMatrix.getDistanceMetres(mainEndBar, bar);
        double fraction = (double) distanceFromStart / (distanceFromStart + distanceFromEnd); // 0 = at start, 1 = at end
        return (int) Math.round(fraction * (numberOfBars - 1));
    }

    /**
     * Scores every candidate bar for the current segment, normalises and weights the errors,
     * ranks the candidateBarsNotInRoute, then randomly picks from the top NUMBER_OF_CANDIDATE_BARS.
     * @param candidateBarsNotInRoute bars not already used elsewhere in the route
     * @param fullRoute the complete route list; null where a bar hasn't been chosen yet
     * @param candidateRouteIndex the route slot index this decision is filling
     * @return the chosen Bar for this midpoint
     */
    private Bar chooseCandidateBar(List<Bar> candidateBarsNotInRoute, List<Bar> fullRoute, int candidateRouteIndex) {
        if (fullRoute.get(candidateRouteIndex) != null) {
            return fullRoute.get(candidateRouteIndex);
        }

        List<double[]> rawErrors = new ArrayList<>();

        for (Bar candidate : candidateBarsNotInRoute) {
            // Build the compact ordered list of already-chosen bars, with the
            // candidate slotted in at its prospective position. nulls skipped.
            // orderedRouteSlots[i] records the ORIGINAL fullRoute slot of the bar
            // at orderedRoute[i], so "stops apart" is a plain subtraction.
            List<Bar> orderedRoute = new ArrayList<>();
            List<Integer> orderedRouteSlots = new ArrayList<>();
            int candidatePosition = -1;
            for (int i = 0; i < fullRoute.size(); i++) {
                if (i == candidateRouteIndex) {
                    candidatePosition = orderedRoute.size();
                    orderedRoute.add(candidate);
                    orderedRouteSlots.add(i);
                    continue;
                }
                Bar barAtIndex = fullRoute.get(i);
                if (barAtIndex != null) {
                    orderedRoute.add(barAtIndex);
                    orderedRouteSlots.add(i);
                }
            }

            double[] prefixDistance = new double[orderedRoute.size()];
            for (int i = 1; i < orderedRoute.size(); i++) {
                prefixDistance[i] = prefixDistance[i - 1]
                        + distanceMatrix.getDistanceMetres(orderedRoute.get(i - 1), orderedRoute.get(i));
            }

            double[] errors = new double[NUM_ERROR_COMPONENTS_BAR];
            errors[0] = getErrorOfDistanceFromMain(prefixDistance);
            errors[1] = getErrorOfDistanceFromOthers(orderedRouteSlots, candidatePosition,
                    candidateRouteIndex, prefixDistance);

            rawErrors.add(errors);
        }

        List<Double> scores = normaliseAndWeightBar(rawErrors);

        List<Bar> ranked = IntStream.range(0, scores.size())
                .boxed()
                .sorted(Comparator.comparingDouble(scores::get))
                .limit(NUMBER_OF_CANDIDATE_BARS)
                .map(candidateBarsNotInRoute::get)
                .toList();

        return ranked.get(random.nextInt(ranked.size()));
    }

    /**
     * Filters generated routes by required bars/themes/features, scores the survivors
     * on true distance and spacing, then randomly picks from the top
     * NUMBER_OF_FINAL_CANDIDATE_ROUTES.
     * @param generatedRoutes every route produced by getChosenBarsRecursively()
     * @return the chosen route
     */
    private ArrayList<Bar> chooseCandidateBarList(List<ArrayList<Bar>> generatedRoutes) {
        List<ArrayList<Bar>> filteredBarLists = generatedRoutes.stream()
                .filter(route -> route.containsAll(particularBars))
                .filter(route -> themes.stream().allMatch(theme ->
                        route.stream().anyMatch(bar -> bar.getTheme() == theme)))
                .filter(route -> features.stream().allMatch(featureType ->
                        route.stream().anyMatch(bar ->
                                bar.getFeatures().stream()
                                        .anyMatch(feature -> feature.featureType == featureType))))
                .filter(route -> Math.abs(calculateTotalDistanceMetres(route) - approximateTotalDistanceInMetres) <= approximateTotalDistanceInMetres * DISTANCE_ERROR)
                .toList();

        HashMap<ArrayList<Bar>, double[]> rawErrors = new HashMap<>();
        for (ArrayList<Bar> barList : filteredBarLists) {
            double[] errors = new double[NUM_ERROR_COMPONENTS_BAR];
            errors[0] = getErrorOfDistance(barList);
            errors[1] = getErrorOfSpacing(barList);
            rawErrors.put(barList, errors);
        }

        Map<ArrayList<Bar>, Double> scores = normaliseAndWeightRoute(rawErrors);

        if (filteredBarLists.isEmpty()) {
            return null;
        }
        List<ArrayList<Bar>> rankedBarLists = filteredBarLists.stream()
                .sorted(Comparator.comparingDouble(scores::get))
                .limit(NUMBER_OF_FINAL_CANDIDATE_ROUTES)
                .toList();

        return rankedBarLists.get(random.nextInt(rankedBarLists.size()));
    }

    /**
     * Min-max normalises each of the raw error components across the whole
     * candidate batch, so a component with larger rawErrors can't dominate a component with naturally smaller
     * rawErrors. Every error component ends up scaled 0 to 1 before weighting; the lower, the better
     * @param rawErrors each candidate mapped to its raw (un-normalised) error values
     * @return each candidate mapped to its final weighted, normalised total error
     */
    private List<Double> normaliseAndWeightBar(List<double[]> rawErrors) {
        double[] min = new double[NUM_ERROR_COMPONENTS_BAR];
        double[] max = new double[NUM_ERROR_COMPONENTS_BAR];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);

        for (double[] errors : rawErrors) {
            for (int i = 0; i < NUM_ERROR_COMPONENTS_BAR; i++) {
                min[i] = Math.min(min[i], errors[i]);
                max[i] = Math.max(max[i], errors[i]);
            }
        }

        double[] weights = {MAIN_DISTANCE_WEIGHT_BAR, OTHERS_DISTANCES_WEIGHT_BAR};

        List<Double> scores = new ArrayList<>();
        for (double[] errors : rawErrors) {
            double total = 0;
            for (int i = 0; i < NUM_ERROR_COMPONENTS_BAR; i++) {
                double range = max[i] - min[i];
                double normalised = range == 0 ? 0 : (errors[i] - min[i]) / range;
                total += weights[i] * normalised;
            }
            scores.add(total);
        }
        return scores;
    }

    /**
     * Min-max normalises and weights the raw route-level errors across a batch of filtered routes.
     * @param rawErrors each route mapped to its raw (un-normalised) error values
     * @return each route mapped to its final weighted, normalised total error
     */
    private Map<ArrayList<Bar>, Double> normaliseAndWeightRoute(HashMap<ArrayList<Bar>, double[]> rawErrors) {
        double[] min = new double[NUM_ERROR_COMPONENTS_ROUTES];
        double[] max = new double[NUM_ERROR_COMPONENTS_ROUTES];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);

        for (double[] errors : rawErrors.values()) {
            for (int i = 0; i < NUM_ERROR_COMPONENTS_ROUTES; i++) {
                min[i] = Math.min(min[i], errors[i]);
                max[i] = Math.max(max[i], errors[i]);
            }
        }

        double[] weights = {MAIN_DISTANCE_WEIGHT_ROUTE, SPACING_WEIGHT_ROUTE};

        Map<ArrayList<Bar>, Double> scores = new HashMap<>();
        for (Map.Entry<ArrayList<Bar>, double[]> entry : rawErrors.entrySet()) {
            double[] errors = entry.getValue();
            double total = 0;
            for (int i = 0; i < NUM_ERROR_COMPONENTS_ROUTES; i++) {
                double range = max[i] - min[i];
                double normalised = range == 0 ? 0 : (errors[i] - min[i]) / range;
                total += weights[i] * normalised;
            }
            scores.put(entry.getKey(), total);
        }
        return scores;
    }

    /**
     * Checks how close the REAL cumulative route distance (walking through every
     * bar already fixed, in true route order, with this candidate inserted at its
     * slot) is to the user's requested total distance.
     * @param prefixDistanceAlongRoute cumulative real distance up to each bar in the ordered route
     * @return error in metres
     */
    private double getErrorOfDistanceFromMain(double[] prefixDistanceAlongRoute) {
        double totalRouteDistanceSoFar = prefixDistanceAlongRoute[prefixDistanceAlongRoute.length - 1];
        return Math.abs(approximateTotalDistanceInMetres - totalRouteDistanceSoFar);
    }

    /**
     * Checks how well-spaced this candidate would be from bars already in the
     * route: if every bar were perfectly evenly spaced, a bar N slots away
     * should be roughly N x (average distance per stop) away. The ACTUAL distance
     * used is the real cumulative route distance between the two slots.
     * @param orderedRouteSlots the orderedRouteIndices which have been filled by a bar i.e. non-null
     * @param candidatePosition the index of the candidate in the list of bars
     * @param candidateRouteIndex the index in the orderedRoute which we are calculating error for
     * @return average error in metres across all already-chosen bars
     */
    private double getErrorOfDistanceFromOthers(List<Integer> orderedRouteSlots, int candidatePosition,
                                                int candidateRouteIndex, double[] prefixDistanceAlongRoute) {
        double averageDistancePerStop = (double) approximateTotalDistanceInMetres / (numberOfBars - 1);
        double cumulativeError = 0;
        int chosenCount = 0;

        for (int i = 0; i < orderedRouteSlots.size(); i++) {
            if (i == candidatePosition) {
                continue;
            }
            double actualDistanceAlongRoute = Math.abs(
                    prefixDistanceAlongRoute[candidatePosition] - prefixDistanceAlongRoute[i]);

            int stopsApart = Math.abs(orderedRouteSlots.get(i) - candidateRouteIndex);
            double expectedDistance = averageDistancePerStop * stopsApart;

            cumulativeError += Math.abs(actualDistanceAlongRoute - expectedDistance);
            chosenCount++;
        }

        return chosenCount == 0 ? 0 : cumulativeError / chosenCount;
    }

    private boolean routeContainsAllParticularBars(List<Bar> route) {
        return new HashSet<>(route).containsAll(particularBars);
    }

    private boolean routeContainsAllThemes(List<Bar> route) {
        return themes.stream().allMatch(theme ->
                route.stream().anyMatch(bar -> theme.equals(bar.getTheme())));
    }

    private boolean routeContainsAllFeatures(List<Bar> route) {
        return features.stream().allMatch(feature ->
                route.stream().anyMatch(bar -> bar.getFeatures().stream().anyMatch(x -> x.featureType.equals(feature))));
    }

    private boolean routeIsApproximateDistance(List<Bar> route) {
        return Math.abs(calculateTotalDistanceMetres(route) - approximateTotalDistanceInMetres)
                        <= approximateTotalDistanceInMetres * DISTANCE_ERROR;
    }

    /**
     * Diagnose why the no valid route is loading<br>
     * Checks for errors with particularBars, themes, features, and approximateTotalDistance
     * @param routes the list of possible routes to filter
     *               through and catch any errors
     * @return the error which is preventing a route from loading
     */
    private String diagnose(List<ArrayList<Bar>> routes) {
        if (!particularBars.isEmpty() && routes.stream().noneMatch(this::routeContainsAllParticularBars)) {
            return "Couldn't fit all your required bars into a route this size.\nTry adjusting the filters.";
        }
        if (!themes.isEmpty() && routes.stream().noneMatch(this::routeContainsAllThemes)) {
            return "Couldn't fit your selected theme(s) into a route.\nTry adjusting the filters.";
        }
        if (!features.isEmpty() && routes.stream().noneMatch(this::routeContainsAllFeatures)) {
            return "Couldn't fit your selected feature(s) into a route.\nTry adjusting the filters.";
        }
        if (routes.stream().noneMatch(this::routeIsApproximateDistance)) {
            return "Couldn't get close to your target distance of "
                    + Math.round(approximateTotalDistanceInMetres / 100.0) / 10.0 + "km.\nTry adjusting the filters.";
        }
        return "Your requirements work individually but not all together. Try dropping one.";
    }

    /**
     * Generates route (using algorithm 3: recursion) using parameters specified when algorithm built
     * @return Route object containing bars and additional route information.
     */
    public Route getRoute() {
        List<ArrayList<Bar>> generatedRoutes = new ArrayList<>(NUMBER_OF_CANDIDATE_ROUTES);
        for (int i = 0; i < NUMBER_OF_CANDIDATE_ROUTES; i++) {
            generatedRoutes.add(getChosenBarsRecursively());
        }

        logger.info("Choosing candidate bars");
        ArrayList<Bar> barsInOrder = chooseCandidateBarList(generatedRoutes);

        if (barsInOrder == null) {
            logger.info("No candidate routes found");
            throw new NoRouteGeneratedException(diagnose(generatedRoutes));
        }

        Route generatedRoute = new Route(barsInOrder);
        logger.info("Route generated: \n{}", generatedRoute);
        return generatedRoute;
    }

    private double getErrorOfDistance(ArrayList<Bar> barList) {
        return Math.abs(calculateTotalDistanceMetres(barList) - approximateTotalDistanceInMetres);
    }

    private double getErrorOfSpacing(ArrayList<Bar> barList) {
        int actualTotal = calculateTotalDistanceMetres(barList);
        double expectedSpacing = (double) actualTotal / (numberOfBars - 1);
        double cumulativeError = 0;
        for (int i = 0; i < barList.size() - 1; i++) {
            cumulativeError += Math.abs(expectedSpacing - distanceMatrix.getDistanceMetres(barList.get(i), barList.get(i + 1)));
        }
        return cumulativeError / numberOfBars;
    }

    private int calculateTotalDistanceMetres(List<Bar> barsInOrder) {
        int distance = 0;
        for (int i = 0; i < barsInOrder.size()-1; i++) {
            distance += distanceMatrix.getDistanceMetres(barsInOrder.get(i), barsInOrder.get(i+1));
        }
        return distance;
    }

}