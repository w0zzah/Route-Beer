package seng202.team5.gui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import seng202.team5.models.Location;
import seng202.team5.services.MainManager;

import java.io.IOException;
import java.util.ArrayList;

public class DataViewController {

    private ObservableList<Location> data;

    @FXML
    private TableView<Location> dataTable;
    @FXML
    private TableColumn<Location, String> locID;
    @FXML
    private TableColumn<Location, String> name;
    @FXML
    private TableColumn<Location, String> description;
    @FXML
    private TableColumn<Location, Integer> latitude;
    @FXML
    private TableColumn<Location, Integer> longitude;
    @FXML
    private TableColumn<Location, String> address;
    @FXML
    private TableColumn<Location, ArrayList<Object>> features;
    @FXML
    private TableColumn<Location, Integer> openingTime;
    @FXML
    private TableColumn<Location, Integer> closingTime;
    private ScreenNavigator navigator;
    private MainManager mainManager;

    public DataViewController() throws IOException { }

    /**
     * Initialises Data View window
     */
    @FXML
    public void initialize() {
        locID.setCellValueFactory(new PropertyValueFactory<>("locID"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        latitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        features.setCellValueFactory(new PropertyValueFactory<>("features"));
        openingTime.setCellValueFactory(new PropertyValueFactory<>("openingTime"));
        closingTime.setCellValueFactory(new PropertyValueFactory<>("closingTime"));

        data = FXCollections.observableArrayList();
        dataTable.setItems(data);

    }

    /**
     * Displays a random location
     */
    @FXML
    public void showRandomLocation() {
        Location randomLocation = mainManager.getRandomBar();
        data.add(randomLocation);
    }

    /**
     * Initialises the data view window services
     * @param navigator The current navigator
     * @param mainManager The current MainManager
     */
    public void initialise(ScreenNavigator navigator, MainManager mainManager) {
        this.navigator = navigator;
        this.mainManager = mainManager;
    }

    /**
     * Returns to setup page
     */
    public void goBack() {
        navigator.goToSetup(false);
    }
}
