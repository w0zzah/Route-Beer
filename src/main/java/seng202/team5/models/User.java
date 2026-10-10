package seng202.team5.models;

import seng202.team5.models.types.UserType;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains user's info for logging in and viewing save/creating new routes.
 * <p>
 * <i>May become changed/deleted for security reasons</i>
 * @author Theo Baker
 */

public class User {

    private final UserType userType;
    private final int id;
    public List<Route> userRoutes;
    private final String username;

    /**
     * The constructor for creating a user with a specified ID,
     * username and user type
     * @param id the unique identifier for the user
     * @param username the username of the user
     * @param userType what type of user it is
     */
    public User(int id, String username, UserType userType) {
        this.id = id;
        this.username = username;
        this.userType = userType;
        this.userRoutes = new ArrayList<>();
    }
    public int getId() { return id; }

    public String getUsername() { return username; }

    public UserType getType() {
        return userType;
    }

    public List<String> getSavedRouteNames() {
        return this.userRoutes.stream().map(Route::getName).toList();
    }
}
