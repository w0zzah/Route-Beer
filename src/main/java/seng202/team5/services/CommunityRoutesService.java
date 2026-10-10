package seng202.team5.services;

import seng202.team5.exceptions.DataAccessException;
import seng202.team5.models.Route;
import seng202.team5.models.RouteRating;
import seng202.team5.models.types.UserType;

import java.util.ArrayList;
import java.util.List;

import static seng202.team5.models.types.UserType.GUEST;

/**
 * Business logic for community routes: loading them with the current
 * user's rating information, and voting.
 */
public class CommunityRoutesService {

    /** Guest users have this id and cannot vote. */
    private static final int GUEST_USER_ID = -1;

    /**
     * A community route paired with its rating data for the current user.
     *
     * @param route the route
     * @param rating vote totals and the current user's vote
     */
    public record CommunityRouteEntry(Route route, RouteRating rating) {
    }

    private final MainManager mainManager;

    /**
     * @param mainManager the application's main manager
     */
    public CommunityRoutesService(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    /**
     * @return true if the current user is signed in and may vote
     */
    public boolean canVote() {
        return currentUserType() != GUEST;
    }

    /**
     * Loads every community route along with its rating. Loads everything
     * before returning so a failure never leaves a half-built list.
     *
     * @return the entries
     * @throws DataAccessException if the data could not be read
     */
    public List<CommunityRouteEntry> loadCommunityRoutes() throws DataAccessException {
        int userId = currentUserId();
        List<CommunityRouteEntry> entries = new ArrayList<>();
        for (Route route : mainManager.getCommunityRoutes()) {
            RouteRating rating = mainManager.routeDAO.getRouteVotes(route.getRouteID(), userId);
            entries.add(new CommunityRouteEntry(route, rating));
        }
        return entries;
    }

    /**
     * Records the current user's vote on a route.
     *
     * @param routeId route being voted on
     * @param voteValue the vote value
     */
    public void vote(int routeId, int voteValue) {
        mainManager.routeDAO.voteRoute(currentUserId(), routeId, voteValue);
    }

    private int currentUserId() {
        return mainManager.getCurrentUser().getId();
    }

    private UserType currentUserType() {
        return mainManager.getCurrentUser().getType();
    }
}