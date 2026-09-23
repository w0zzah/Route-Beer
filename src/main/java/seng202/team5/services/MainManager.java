package seng202.team5.services;

import seng202.team5.data.RouteDAO;
import seng202.team5.data.UserDAO;
import seng202.team5.gui.ScreenNavigator;
import seng202.team5.models.Bar;
import seng202.team5.models.Route;
import seng202.team5.models.User;

import java.util.List;

/**
 * Class to handle the main application information. This acts as an MVC controller, taking requests from the view and
 * using relevant layer actions to complete them
 * @author Neela van Gerwen
 */
public class MainManager {

    public final LocationManager locationManager;
    public Route currentRoute;
    public User currentUser;
    public UserService currentUserService;
    public RouteDAO routeDAO;
    public UserDAO userDAO;

    /**
     * Creates a new MainManager object and ties it to a new LocationManager object
     */
    public MainManager() {
        this.locationManager = new LocationManager();
        this.routeDAO = new RouteDAO(this);
        this.userDAO = new UserDAO();
    }
    /**
     * Returns a random location from the LocationManager
     * @return a random Location object
     */
    public Bar getRandomBar() {
        return locationManager.generateRandomBar();
    }

    public void setCurrentUser(User user, UserService userService) {
        this.currentUser = user;
        this.currentUserService = userService;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public UserService getCurrentUserService() {
        return currentUserService;
    }

    public List<Bar> getBars() {

        return locationManager.getBars();
    }

    public void setCurrentRoute(Route route) {
        this.currentRoute = route;
    }

    public Route getCurrentRoute() {
        return this.currentRoute;
    }


}
