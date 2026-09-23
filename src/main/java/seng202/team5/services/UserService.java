package seng202.team5.services;

import seng202.team5.data.RouteDAO;
import seng202.team5.models.Route;

import java.util.List;

/**
 * Provides access to user services and the main application layer
 */
public class UserService {

    MainManager mainManager;

    /**
     * Creates a new UserService from the associated mainManager
     * @param mainManager the MainManager associated with the account
     */
    public UserService(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    public MainManager getMainManager() {
        return mainManager;
    }

    public List<Route> loadCommunityRoutes() {
        return mainManager.routeDAO.getCommunityRoutes();
    }

}
