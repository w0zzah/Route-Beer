package seng202.team5.services;

import seng202.team5.data.RouteDAO;
import seng202.team5.data.UserDAO;
import seng202.team5.gui.widgets.VenueInfoWidget;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.Route;
import seng202.team5.models.User;
import seng202.team5.models.locations.FastFood;
import seng202.team5.services.route_generation.LocationManager;


import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the main application information. This acts as an MVC controller, taking requests from the view and
 * using relevant layer actions to complete them
 * @author Neela van Gerwen
 */
public class MainManager {

    public final LocationManager locationManager;
    public Route currentRoute;
    public List<Route> generatedRoutes;
    public User currentUser;
    public RouteDAO routeDAO;
    public UserDAO userDAO;
    public VenueInfoWidget venueInfoWidget;

    /**
     * Creates a new MainManager object and ties it to a new LocationManager object
     */
    public MainManager() {
        this.locationManager = new LocationManager();
        this.routeDAO = new RouteDAO(this);
        this.userDAO = new UserDAO();
        this.generatedRoutes = new ArrayList<>();
        this.venueInfoWidget = new VenueInfoWidget();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return this.currentUser;
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
    public void addGeneratedRoute(Route route) {
        generatedRoutes.add(route);
    }

    public void clearGeneratedRoutes() {
        generatedRoutes.clear();
    }

    public Route getGeneratedRoute(int n) {
        return generatedRoutes.get(n);
    }
    public List<Route> getCommunityRoutes() {
        return routeDAO.getCommunityRoutes();
    }

    public void deleteUser() { userDAO.deleteUser(currentUser.getUsername()); }
    public List<FastFood> getFastFood(){return locationManager.getFastFood();}

    public List<Route> getUserRoutes() {
        return routeDAO.getUserRoutes(this.currentUser.getId());
    }

    public void setUserRoutes() {
        currentUser.userRoutes = this.getUserRoutes();
    }

    public List<String> getSavedRouteNames() {
        return currentUser.userRoutes.stream().map(Route::getName).toList();
    }

    public int getUserId() {
        return this.currentUser.getId();
    }
}