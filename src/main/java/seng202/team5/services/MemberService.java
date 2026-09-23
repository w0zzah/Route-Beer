package seng202.team5.services;

import seng202.team5.data.*;
import seng202.team5.models.Route;
import seng202.team5.models.User;

import java.util.List;

/**
 * Class to handle all actions for Members. This acts as an MVC controller, managing functionality tied to Members
 */
public class MemberService extends UserService{

    User user;
    /**
     * Creates a new MemberService and calls the loadMemberRoutes method
     */
    public MemberService(MainManager mainManager) {
        super(mainManager);
    }

    /**
     * Loads the saved routes for a Member and stores them in memberRoutes
     */
    public List<Route> loadMemberRoutes(int user_id) {
        return mainManager.routeDAO.getUserRoutes(user_id);
    }

}
