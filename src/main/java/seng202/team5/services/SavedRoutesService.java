package seng202.team5.services;

import seng202.team5.gui.util.DialogUtil;
import seng202.team5.models.Route;

import java.util.List;

/**
 * Business logic for a user's saved routes. Wraps the route DAO so the
 * GUI never touches persistence directly.
 */
public class SavedRoutesService {

    /** Name given to a route saved without one. */
    public static final String DEFAULT_ROUTE_NAME = "GENERATED ROUTE";

    /** Outcome of a save attempt. */
    public enum SaveResult {
        SAVED,
        NAME_TAKEN,
        FAILED
    }

    private final MainManager mainManager;

    /**
     * @param mainManager the application's main manager
     */
    public SavedRoutesService(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    /**
     * Checks whether the user already has a saved route with this name.
     *
     * @param name candidate name (compared after trimming)
     * @return true if taken; false if free or blank
     */
    public boolean isNameTaken(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String trimmed = name.trim();
        for (String saved : mainManager.getSavedRouteNames()) {
            if (saved != null && saved.trim().equals(trimmed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the name and description to the route and saves it for the current user.
     * A blank name becomes {@link #DEFAULT_ROUTE_NAME}. A name already in use is rejected.
     * On success the cached list of user routes is refreshed.
     *
     * @param route route to save
     * @param name desired name (may be blank)
     * @param description desired description (may be blank)
     * @return the outcome
     */
    public SaveResult saveRoute(Route route, String name, String description) {
        String finalName = (name == null || name.isBlank()) ? DEFAULT_ROUTE_NAME : name;
        if (isNameTaken(finalName)) {
            return SaveResult.NAME_TAKEN;
        }
        route.setName(finalName);
        route.setDesc(description);

        boolean saved = mainManager.routeDAO.saveRoute(route, mainManager.getCurrentUser().getId());
        if (!saved) {
            return SaveResult.FAILED;
        }
        mainManager.setUserRoutes();
        return SaveResult.SAVED;
    }

    /**
     * @return the current user's saved routes
     */
    public List<Route> getSavedRoutes() {
        return mainManager.getUserRoutes();
    }

    /**
     * Deletes a saved route and refreshes the cached list.
     * @param route route to delete
     */
    public void deleteRoute(Route route) {
        mainManager.routeDAO.deleteSavedRoute(route.getRouteID());
        mainManager.setUserRoutes();
    }

    /**
     * Publishes a saved route to the community.
     * @param route route to publish
     */
    public void publishRoute(Route route) {
        mainManager.routeDAO.publishCommunityRoute(route, mainManager.getUserId());
        DialogUtil.info("Route Published", "Route Successfully Published!");
    }
}
