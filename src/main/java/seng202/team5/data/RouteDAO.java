package seng202.team5.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.NoRouteRatings;
import seng202.team5.models.*;
import seng202.team5.models.locations.Bar;
import seng202.team5.models.RouteRating;
import seng202.team5.models.locations.Location;
import seng202.team5.services.MainManager;
import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exchanges information between Java and routes table in Database
 */
public class RouteDAO {

    private static final Gson GSON = new Gson();
    private static final Type ID_LIST_TYPE = new TypeToken<ArrayList<String>>() {}.getType();
    private static final Logger logger = LogManager.getLogger(RouteDAO.class);

    // Instance variable injected via constructor
    private final MainManager mainManager;

    /**
     * Constructs a new RouteDAO.
     *
     * @param mainManager The main manager required for parsing locations.
     */
    public RouteDAO(MainManager mainManager) {
        if (mainManager == null || mainManager.locationManager == null) {
            throw new IllegalArgumentException("MainManager and its LocationManager cannot be null");
        }
        this.mainManager = mainManager;
    }

    /**
     * Save a route to the user
     *
     * @param route   Route that the user wants to save
     * @param user_id User's ID number on the database
     * @return True if no error codes were return from statement.executeUpdate()
     */
    public boolean saveRoute(Route route, int user_id) {
        String query = """
                INSERT INTO routes (user_id, route_name, route_description, total_distance, created_date, route_locations, estimated_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        List<Route> userRoutes = getUserRoutes(user_id);
        for (Route UserRoute : userRoutes) {
            if (Objects.equals(UserRoute.getName(), route.getName())) {
                return false;
            }
        }
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, user_id);
            statement.setString(2, route.getName());
            statement.setString(3, route.getDescription());
            statement.setDouble(4, route.getTotalDistanceKm());
            statement.setDate(5, Date.valueOf(route.getCreatedDate()));
            statement.setString(6, toIdListJson(route.getBars()));
            statement.setInt(7, route.getEstimatedTime());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        route.setRouteID(generatedId);
                    }
                }
                connection.close();
                return true;
            }
            connection.close();
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save route", e);
        }
    }

    public boolean publishCommunityRoute(Route route, int user_id) {
        String query = """
                UPDATE routes
                SET is_community = 1
                WHERE user_id = ? AND route_name = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user_id);
            statement.setString(2, route.getName());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to publish community route", e);
        }
    }

    /**
     * Gets user routes.
     *
     * @param userId the user id
     * @return the user routes
     */
    public List<Route> getUserRoutes(int userId) {
        return fetchRoutes("""
                SELECT *
                FROM routes join users on user_id = users.id
                WHERE user_id = ?""", userId);
    }

    /**
     * Gets community routes.
     *
     * @return the community routes
     */
    public List<Route> getCommunityRoutes() {
        return fetchRoutes("""
                SELECT *
                FROM routes join users on user_id = users.id
                WHERE is_community = 1""", -1);
    }

    /**
     * Delete saved route boolean.
     *
     * @param routeId the route id
     * @return the boolean
     */
    public boolean deleteSavedRoute(int routeId) {
        String query = "DELETE FROM routes WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, routeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete saved route", e);
        }
    }

    private List<Route> fetchRoutes(String query, int parameterId) {
        List<Route> routes = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (parameterId != -1) {
                statement.setInt(1, parameterId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String routeName = resultSet.getString("route_name");
                    String description = resultSet.getString("route_description");
                    Date sqlDate = resultSet.getDate("created_date");
                    LocalDate createdDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    String idListJson = resultSet.getString("route_locations");
                    String ownerUsername = resultSet.getString("username");

                    List<? extends Location> locations = parseLocations(idListJson);

                    Route route = new Route(
                            id,
                            routeName,
                            description,
                            (List<Bar>) locations,
                            createdDate,
                            ownerUsername
                    );
                    routes.add(route);
                }
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error fetching routes from database", e);
            throw new DataAccessException("Failed to fetch routes", e);
        }
        return routes;
    }

    private String toIdListJson(List<? extends Location> locations) {
        ArrayList<String> ids = new ArrayList<>();
        for (Location location : locations) {
            ids.add(location.getLocID());
        }
        return GSON.toJson(ids);
    }

    private List<Location> parseLocations(String idListJson) {
        List<Location> fixedList = new ArrayList<>();
        if (idListJson == null || idListJson.isEmpty()) {
            return fixedList;
        }

        ArrayList<String> ids = GSON.fromJson(idListJson, ID_LIST_TYPE);
        for (String id : ids) {
            Location location = mainManager.locationManager.findById(id);
            if (location != null) {
                fixedList.add(location);
            } else {
                logger.error("Could not find location with id {}", id);
            }
        }
        return fixedList;
    }

    public int getRouteVote(int route_id, int vote) {
        int totalVotes = 0;
        String query = """
                SELECT count(vote)
                FROM route_ratings
                WHERE route_id = ? and vote = ?;
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, route_id);
            statement.setInt(2, vote);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalVotes = resultSet.getInt("count(vote)");
                }
            } catch (SQLException e) {
                throw new NoRouteRatings(e.getMessage());
            }
            connection.close();
            return totalVotes;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void voteRoute(int user_id, int route_id, int vote) {
        String query = """
                INSERT INTO route_ratings (user_id, route_id, vote)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE vote = VALUES(vote)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user_id);
            statement.setInt(2, route_id);
            statement.setInt(3, vote);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public List<Integer> getUserVotes(int userID, int vote) {
        List<Integer> routeIds = new ArrayList<>();

        String query = """
            SELECT route_id, vote
            FROM route_ratings
            WHERE user_id = ? AND vote = ?;
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userID);
            statement.setInt(2, vote);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    routeIds.add(resultSet.getInt("route_id"));

                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching routes from database", e);
            throw new DataAccessException("Failed to fetch routes", e);
        }
        return routeIds;
    }

    /**
     * Gets the count of upvotes, downvotes
     * and the given user's rating for a specific route
     * @param route_id id of the target route
     * @param user_id id of the current user
     * @return the RouteRating object containing vote data
     */
    public RouteRating getRouteVotes(int route_id, int user_id) {
        int upvotes = 0;
        int downvotes = 0;
        int userVote = 0;

        String voteCountQuery = """
            SELECT vote, count(*) AS vote_count
            FROM route_ratings
            WHERE route_id = ? AND vote !=0
            GROUP BY vote;
            """;


        String userVoteQuery = """
            SELECT vote
            FROM route_ratings
            WHERE user_id = ? AND route_id = ?;
            """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(voteCountQuery)) {
                statement.setInt(1, route_id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int vote = resultSet.getInt("vote");
                        int count = resultSet.getInt("vote_count");
                        if (vote == 1) {
                            upvotes = count;
                        } else if (vote == -1) {
                            downvotes = count;
                        }
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(userVoteQuery)) {
                statement.setInt(1, user_id);
                statement.setInt(2, route_id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        userVote = resultSet.getInt("vote");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching routes from database", e);
            throw new DataAccessException("Failed to fetch routes", e);
        }
        return new RouteRating(upvotes, downvotes, userVote);
    }

}