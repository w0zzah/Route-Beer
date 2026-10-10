package seng202.team5.gui.tab_pane_controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;
import seng202.team5.exceptions.InvalidParametersException;
import seng202.team5.exceptions.InvalidParametersReason;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.gui.util.DialogUtil;
import seng202.team5.gui.util.FieldErrorUtil;
import seng202.team5.gui.util.InputFormatters;
import seng202.team5.gui.widgets.BarCardWidget;
import seng202.team5.models.Route;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.types.FeatureType;
import seng202.team5.models.types.Theme;
import seng202.team5.services.RouteBuilderService;
import seng202.team5.models.RouteFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RouteBuilderTabController {

    @FXML
    private TextField startBarField;
    @FXML
    private TextField endBarField;
    @FXML
    private TextField approxDistanceField;
    @FXML
    private TextField particularBarsSearchField;
    @FXML
    private Label startBarErrorLabel;
    @FXML
    private Label endBarErrorLabel;
    @FXML
    private Label particularBarsErrorLabel;
    @FXML
    private Label themesErrorLabel;
    @FXML
    private Spinner<Integer> numBarsSpinner;
    @FXML
    private CheckComboBox<FeatureType> barFeaturesCheckComboBox;
    @FXML
    private CheckComboBox<Theme> barThemesCheckComboBox;
    @FXML
    private VBox particularBarsVBox, approxDistanceVBox;
    @FXML
    private ScrollPane particularBarsScrollpane;
    @FXML
    private Button generateRouteButton;

    private Task<Route> routeTask;
    private String generateButtonText;
    private Node generateButtonGraphic;

    private RouteBuilderService routeBuilderService;
    private TabPaneCoordinator coordinator;
    private Consumer<Bar> showVenueInfo;

    private final ObservableList<Bar> selectedParticularBars = FXCollections.observableArrayList();

    /**
     * Where and how to show a validation failure.
     */
    private record FieldError(Node field, Label label, String message) {
    }

    /**
     * Initialises the controller with the necessary service classes needed
     * @param routeBuilderService the service class for generating routes
     * @param coordinator the coordinator for showing the generated route on the map
     * @param showVenueInfo the helper for including particular bars and showing them in the builder
     */
    public void initialise(RouteBuilderService routeBuilderService, TabPaneCoordinator coordinator,
                           Consumer<Bar> showVenueInfo) {
        this.routeBuilderService = routeBuilderService;
        this.coordinator = coordinator;
        this.showVenueInfo = showVenueInfo;
    }

    /**
     * View-only setup: bindings, combo box items and input formatters.
     */
    public void initialiseControls() {
        particularBarsScrollpane.visibleProperty().bind(Bindings.isNotEmpty(selectedParticularBars));
        particularBarsScrollpane.managedProperty().bind(particularBarsScrollpane.visibleProperty());

        barFeaturesCheckComboBox.getItems().addAll(FeatureType.values());
        barThemesCheckComboBox.getItems().addAll(Theme.values());
        approxDistanceField.setTextFormatter(InputFormatters.decimalOneDp());

        distanceTooltip();
    }

    private void distanceTooltip() {
        Tooltip tooltip = new Tooltip("Approximate distance");
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(approxDistanceVBox, tooltip);
    }

    /**
     * Configures the number-of-bars spinner.
     */
    public void initialiseSpinner() {
        numBarsSpinner.setValueFactory(new FloorSpinnerValueFactory());
        numBarsSpinner.setEditable(true);
        numBarsSpinner.getEditor().setPromptText(
                RouteBuilderService.MIN_BARS + "-" + RouteBuilderService.MAX_BARS);
        numBarsSpinner.getEditor().setTextFormatter(new TextFormatter<>(
                new IntegerStringConverter(),
                null,
                c -> c.getControlNewText().matches("[2-9]?") ? c : null)
        );
    }

    /** Kills the JVM if the application exits while route stuck on generation  */
    private static final ExecutorService ROUTE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "route-generation");
        thread.setDaemon(true);
        return thread;
    });


    /**
     * Adds autocomplete to the bar name fields.
     */
    public void setupParameters() {
        Collection<String> barNames = routeBuilderService.getBarNames();
        TextFields.bindAutoCompletion(startBarField, barNames);
        TextFields.bindAutoCompletion(endBarField, barNames);
        TextFields.bindAutoCompletion(particularBarsSearchField, barNames);
    }

    /**
     *Collects the inputs on FX thread, then asks the service for a route on background thread
     * Results and errors are handled back on the FX thread.
     */
    @FXML
    public void generateRoute() {
        if (routeTask != null && routeTask.isRunning()) {
            return;
        }
        clearErrors();

        final RouteFormat format = readRouteFormat();

        Task<Route> task = new Task<>() {
            @Override
            protected Route call() throws Exception {
                return routeBuilderService.generateRoute(format);
            }
        };

        task.setOnSucceeded(event -> {
            setGenerating(false);
            coordinator.showGeneratedRoute(task.getValue());
        });
        task.setOnFailed(event -> {
            setGenerating(false);
            handleGenerationFailure(task.getException());
        });
        task.setOnCancelled(event -> setGenerating(false));

        routeTask = task;
        setGenerating(true);
        ROUTE_EXECUTOR.execute(task);
    }

    /**
     * Handles error throwing after generation failure
     */
    private void handleGenerationFailure(Throwable throwable) {
        if (throwable instanceof InvalidParametersException e) {
            FieldError error = toFieldError(e.getReason());
            if (error != null) {
                FieldErrorUtil.show(error.field(), error.label(), error.message());
            } else {
                DialogUtil.error(generateRouteButton.getScene().getWindow(), "Route not generated",
                        "Invalid parameters", String.valueOf(e.getReason()));
            }
        } else if (throwable instanceof NoRouteGeneratedException e) {
            DialogUtil.error(generateRouteButton.getScene().getWindow(), "Route not generated",
                    "Could not build a route", e.getMessage());
        } else {
            // Anything unexpected would otherwise vanish silently inside the Task
            DialogUtil.error(generateRouteButton.getScene().getWindow(), "Route not generated",
                    "Unexpected error", String.valueOf(throwable));
        }
    }

    /**
     * Set loading state for generate button.
     * @param generating true while a route is being generated
     */
    private void setGenerating(boolean generating) {
        if (generating) {
            generateButtonText = generateRouteButton.getText();
            generateButtonGraphic = generateRouteButton.getGraphic();

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setPrefSize(16, 16);
            generateRouteButton.setGraphic(spinner);
            generateRouteButton.setContentDisplay(ContentDisplay.LEFT);
            generateRouteButton.setText("Generating...");
            generateRouteButton.setDisable(true);
        } else {
            generateRouteButton.setText(generateButtonText);
            generateRouteButton.setGraphic(generateButtonGraphic);
            generateRouteButton.setDisable(false);
        }
    }

    /**
     * Adds a particular bar to the route builder view so the user
     * has feedback that the bar will be included in the route
     */
    @FXML
    public void addParticularBar() {
        Bar bar = routeBuilderService.findBar(particularBarsSearchField.getText());
        if (bar != null && !selectedParticularBars.contains(bar)) {
            selectedParticularBars.add(bar);
            new BarCardWidget().addParticularBarCard(bar, particularBarsVBox,
                    selectedParticularBars::remove, b -> showVenueInfo.accept(b));
            particularBarsSearchField.clear();
        }
    }

    /**
     * Reads current value of route attributes and converts that to an {@link RouteFormat} record
     * The values are passed on as they are entered so teh service controls the logic
     * @return a {@link RouteFormat} record of the route builder parameters
     */
    private RouteFormat readRouteFormat() {
        return new RouteFormat(
                startBarField.getText(),
                endBarField.getText(),
                readNumBars(),
                routeBuilderService.parseDistance(approxDistanceField.getText()),
                List.copyOf(barThemesCheckComboBox.getCheckModel().getCheckedItems()),
                List.copyOf(barFeaturesCheckComboBox.getCheckModel().getCheckedItems()),
                new ArrayList<>(selectedParticularBars));
    }

    public Double parseTextFieldToNullableDouble(String textFieldInput) {
        if (textFieldInput == null || textFieldInput.trim().isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(textFieldInput.trim());
        } catch (NumberFormatException _) {
            return null;
        }
    }

    /**
     * Reads the value in teh numBarSpinner
     * @return 0 if the spinner is null, otherwise return the spinner value
     */
    private int readNumBars() {
        Integer value = numBarsSpinner.getValue();
        return value == null ? 0 : value;
    }

    /**
     * Links a validation failure reason to the field and message to user
     * @param reason the reason for the error
     * @return a {@link FieldError} record of the error
     */
    private FieldError toFieldError(InvalidParametersReason reason) {
        return switch (reason) {
            case START_BAR_EMPTY -> new FieldError(startBarField, startBarErrorLabel, "Must select a start bar");
            case START_BAR_DOESNT_EXIST -> new FieldError(startBarField, startBarErrorLabel, "Bar does not exist");
            case END_BAR_DOESNT_EXIST -> new FieldError(endBarField, endBarErrorLabel, "Bar does not exist");
            case TOO_MANY_PARTICULAR_BARS -> new FieldError(particularBarsSearchField, particularBarsErrorLabel,
                    "Too many particular bars selected");
            case TOO_MANY_THEMES -> new FieldError(barThemesCheckComboBox, themesErrorLabel,
                    "Too many themes selected");
            default -> null;
        };
    }

    /**
     * clears errors given to user
     */
    private void clearErrors() {
        FieldErrorUtil.clearBorders(startBarField, endBarField, particularBarsSearchField,
                barThemesCheckComboBox);
        FieldErrorUtil.clearTexts(startBarErrorLabel, endBarErrorLabel,
                particularBarsErrorLabel, themesErrorLabel);
    }
}