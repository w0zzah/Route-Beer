package seng202.team5.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;
import seng202.team5.data.RouteDAO;
import seng202.team5.exceptions.NoRouteGeneratedException;
import seng202.team5.exceptions.NoStartBarException;
import seng202.team5.models.*;
import seng202.team5.services.MainManager;
import seng202.team5.services.MemberService;
import seng202.team5.services.RouteService;
import seng202.team5.services.UserService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TabPaneController {

    @FXML
    private VBox savedRouteVBox, communityRouteVBox, routeBarsVBox, alcoholSafetyBox;
    @FXML
    private GridPane routeInfoGrid;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private AnchorPane leftAnchorPane, alcoholSafetyPopupLeft;
    @FXML
    private CheckComboBox<FeatureType> barFeaturesCheckComboBox;
    @FXML
    private CheckComboBox<Theme> barThemesCheckComboBox;
    @FXML
    private Label walkingDistanceLabel, estTimeLabel;
    @FXML
    private TextField startLocationField, endLocationField, approxDistanceField;
    @FXML
    private Spinner<Integer> numBarsSpinner;
    @FXML
    private Button generateRouteButton;
    @FXML
    private TextField routeNameField;
    @FXML
    private TextArea routeDescriptionField;
    @FXML
    private ScrollPane savedScrollPane;
    @FXML
    private Tab communityTab;


    private MainManager mainManager;
    private MapController mapController;
    private ScreenNavigator navigator;

    /**
     * No args constructor for controller
     */
    public TabPaneController() {}

    @FXML
    public void returnToSetup() {
        navigator.goToSetup(false);
    }

    /**
     * Initialise the tab pane
     * @param mainManager Current main manager
     * @param mapController Current map controller
     * @param navigator Current screen navigator
     */
    public void initialise(MainManager mainManager, MapController mapController, ScreenNavigator navigator) {
        this.mainManager = mainManager;
        this.mapController = mapController;
        this.navigator = navigator;
    }

    /**
     * Initialise tabs and set their dimensions
     */
    public void initialiseTabs() {

        // Initialises tab width to responsively fill pane width
        mainTabPane.tabMinWidthProperty().bind(
                mainTabPane.widthProperty()
                        .divide(mainTabPane.getTabs().size())
                        .subtract(31) // Subtract offset to account for borders
        );

        // Get screen dimensions
        var visualBounds = Screen.getPrimary().getVisualBounds();
        double thirdScreenWidth = (visualBounds.getWidth()) / 3;
        double tabHeight = (visualBounds.getHeight()) / 2.5;

        leftAnchorPane.setMinWidth(thirdScreenWidth);
        mainTabPane.setMinHeight(tabHeight);
        routeInfoGrid.setMaxHeight(visualBounds.getHeight() - tabHeight);
        savedRouteVBox.maxWidthProperty().bind(savedScrollPane.widthProperty());

        routeInfoGrid.setVisible(false);

        barFeaturesCheckComboBox.getItems().addAll(FeatureType.values());
        barThemesCheckComboBox.getItems().addAll(Theme.values());

        loadAlcoholSafetyPopupLeft();

    }

    /**
     * Displays and populates the route info grid
     * @param route The route being displayed
     */
    public void displayRouteDetails(Route route) {
        // Clear route info cell
        routeInfoGrid.setVisible(true);
        routeBarsVBox.getChildren().clear();
        loadBarCards(route, routeBarsVBox);
        alcoholSafetyBox.getChildren().clear();
    }

    public void displayRouteName(Route route) {
        routeNameField.setText(route.getName());
        routeDescriptionField.setText(route.getDescription());
    }

    /**
     * Adds bar cards to given container
     * @param route The route being represented
     * @param destination Where the bar cards will be placed
     */
    public void loadBarCards(Route route, VBox destination) {
        if (route != null) {
            walkingDistanceLabel.setText(String.valueOf(route.getTotalDistanceKm()));
            estTimeLabel.setText(String.valueOf(route.getEstimatedTime()/60));
            int barNum = 1;
            for (Bar bar : route.getBars()) {
                BarCardController barCardController = new BarCardController();
                barCardController.addBarCard(bar, destination, barNum, route.getNumStops());
                barNum++;
            }
        }
    }

    /**
     * Grabs values from GUI and passes them
     * through to RouteBuilder to create a new route
     */
    @FXML
    public void generateRoute() {
        Bar startBar = mainManager.locationManager.findBarByName(startLocationField.getText());
        Bar endBar = mainManager.locationManager.findBarByName(endLocationField.getText());
        int numberOfBars;
        try {
            numberOfBars = numBarsSpinner.getValue();
        } catch (NullPointerException e) {
            numberOfBars = 0;
        }
        String maxDistance = approxDistanceField.getText();
        Double approximateDistanceInKm = !Objects.equals(maxDistance, "") ? Double.parseDouble(maxDistance) : null;
        List<Theme> themes = barThemesCheckComboBox.getCheckModel().getCheckedItems();
        List<FeatureType> features = barFeaturesCheckComboBox.getCheckModel().getCheckedItems();


        try {
            RouteService.generateRoute(mainManager, startBar, endBar, numberOfBars, approximateDistanceInKm, themes, features);
            Route generatedRoute = mainManager.getCurrentRoute();
            List<Bar> chosenBars = generatedRoute.getBars();
            displayRouteDetails(generatedRoute);
            mapController.displayRoute(chosenBars);

        } catch (NoStartBarException e) {
            showError("Invalid start location", "Please pick a start location from the available bars.");
        } catch (NoRouteGeneratedException e) {
            showError("Could not build a route", e.getMessage());
        }
    }

    /**
     * Calls method to take map snapshot and
     * launches the export window
     */
    @FXML
    public void callSaveRoute() {
        User currentUser = mainManager.getCurrentUser();
        switch (currentUser.getType()) {
            case GUEST -> {
                // must be signed in to save Route
            }
            case MEMBER -> {
                Route route = mainManager.getCurrentRoute();
                updateRouteDetails(route);
                if (route.getName() == null || route.getName().isEmpty()) {
                    route.setName("GENERATED ROUTE");
                }
                mainManager.routeDAO.saveRoute(route, currentUser.getId());
                loadSavedRoutes((MemberService) mainManager.getCurrentUserService(), currentUser);
            }
        }
    }

    @FXML
    public void exportRoute() {
        Route currentRoute = mainManager.getCurrentRoute();
        List<Bar> routeBars = currentRoute.getBars();
        updateRouteDetails(currentRoute);

        // Reposition the map and get snapshot
        mapController.displayRoute(routeBars);
        updateRouteDetails(currentRoute);
        WritableImage mapSnapshot = mapController.takeMapSnapshot();
        navigator.launchExportWindow(mapSnapshot, currentRoute);
    }

    public void updateRouteDetails(Route route) {
        route.setName(routeNameField.getText());
        route.setDesc(routeDescriptionField.getText());
    }

    @FXML
    public void clearRoute() {
        mapController.unloadRoute();
        routeBarsVBox.getChildren().clear();
        routeInfoGrid.setVisible(false);
        loadAlcoholSafetyPopupLeft();

    }

    /**
     * Hides the spinner text when the value is 1
     * Shows spinner text otherwise
     */
    public void initialiseSpinner() {
        numBarsSpinner.setValueFactory(new FloorSpinnerValueFactory());
        numBarsSpinner.setEditable(true);
        numBarsSpinner.getEditor().setPromptText("2-9");

        numBarsSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                StringConverter<Integer> converter = numBarsSpinner.getValueFactory().getConverter();
                String text = numBarsSpinner.getEditor().getText();
                Integer converted = converter.fromString(text);

                numBarsSpinner.getValueFactory().setValue(converted);
                numBarsSpinner.getEditor().setText(converter.toString(converted));
            }
        });

    }



    /**
     * Adds autocomplete to the location fields
     */
    public void setupParameters() {
        Collection<String> barNames = mainManager.locationManager.getBarNames();
        TextFields.bindAutoCompletion(startLocationField, barNames);
        TextFields.bindAutoCompletion(endLocationField, barNames);

    }

    /**
     * Loads route cards for each saved route tied to the current member
     */
    public void loadSavedRoutes(MemberService memberService, User currentMember) {
        savedRouteVBox.getChildren().clear();
        for (Route route : memberService.loadMemberRoutes(currentMember.getId())) {
            RouteCardController.addRouteCard(route, savedRouteVBox, mapController, this, currentMember, mainManager);
        }
    }

    /**
     * Loads community routes from the database
     * @param userService The current user
     */
    public void loadCommunityRoutes(UserService userService, User currentMember) {
        communityRouteVBox.getChildren().clear();

        for (Route route : userService.loadCommunityRoutes()) {
            RouteCardController.addRouteCard(route, communityRouteVBox, mapController, this, currentMember, mainManager);
        }
    }

    /**
     * Shows given error as an alert
     * @param header error header
     * @param message error message
     */
    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Route not generated");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.initOwner(generateRouteButton.getScene().getWindow());
        alert.showAndWait();
    }

    /**
     * Loads the alcohol safety popup in the tab pane
     */
    private void loadAlcoholSafetyPopupLeft() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/alcohol_safety_popup_left.fxml"));
        try {
            alcoholSafetyPopupLeft = loader.load();
            alcoholSafetyBox.setVisible(true);
            alcoholSafetyBox.getChildren().add(alcoholSafetyPopupLeft);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Loads community routes when community tab is pressed
     */
    @FXML
    private void handleTabSelect() {
        if (communityTab.isSelected()) {
            loadCommunityRoutes(mainManager.getCurrentUserService(), mainManager.getCurrentUser());
        }
    }


}
