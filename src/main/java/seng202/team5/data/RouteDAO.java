package seng202.team5.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.models.*;
import seng202.team5.services.MainManager;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                return true;
            }
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
        return fetchRoutes("SELECT * FROM routes WHERE user_id = ?", userId);
    }

    /**
     * Gets community routes.
     *
     * @return the community routes
     */
    public List<Route> getCommunityRoutes() {
        return fetchRoutes("SELECT * FROM routes WHERE is_community = 1", -1);
    }

    /**
     * Delete community route boolean.
     *
     * @param routeId the route id
     * @param user    the user
     * @return the boolean
     */
    public boolean deleteCommunityRoute(int routeId, User user) {

        if (user.getType() != UserType.ADMIN) {
            logger.warn("User {} attempted to delete a community route without admin privileges.", user.getUsername());
            return false;
        }

        String query = "DELETE FROM routes WHERE id = ? AND is_community = 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, routeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete community route", e);
        }
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

                    List<? extends Location> locations = parseLocations(idListJson);

                    Route route = new Route(
                            id,
                            routeName,
                            description,
                            (List<Bar>) locations,
                            createdDate
                    );
                    routes.add(route);
                }
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
}