package seng202.team5.unittests.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import seng202.team5.data.DatabaseConnection;
import seng202.team5.data.RouteDAO;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.exceptions.NoRouteRatings;
import seng202.team5.models.Route;
import seng202.team5.models.RouteRating;
import seng202.team5.models.locations.Bar;
import seng202.team5.services.MainManager;
import seng202.team5.services.route_generation.LocationManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RouteDAOTest {

    private Connection mockConnection;

    /** Statement returned by prepareStatement(String): SELECT / UPDATE / DELETE queries. */
    private PreparedStatement mockStatement;

    /** Statement returned by prepareStatement(String, RETURN_GENERATED_KEYS): the INSERT in saveRoute. */
    private PreparedStatement mockInsertStatement;

    private ResultSet mockResultSet;
    private ResultSet mockGeneratedKeys;

    private MockedStatic<DatabaseConnection> mockedDbConnection;

    private MainManager mockMainManager;
    private LocationManager mockLocationManager;

    private RouteDAO routeDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockInsertStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockGeneratedKeys = mock(ResultSet.class);

        mockedDbConnection = mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockInsertStatement);

        // Default: queries return an empty result set (e.g. the user has no existing routes)
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        mockMainManager = mock(MainManager.class);
        mockLocationManager = mock(LocationManager.class);
        injectLocationManager(mockMainManager, mockLocationManager);

        routeDAO = new RouteDAO(mockMainManager);
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
    }

    private static void injectLocationManager(MainManager mainManager, LocationManager locationManager) {
        try {
            Field field = MainManager.class.getDeclaredField("locationManager");
            field.setAccessible(true);
            field.set(mainManager, locationManager);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not inject mock LocationManager for tests", e);
        }
    }

    private Route buildMockRoute() {
        Route route = mock(Route.class);
        Bar bar = mock(Bar.class);
        when(bar.getLocID()).thenReturn("bar-1");
        when(route.getName()).thenReturn("Friday Night Crawl");
        when(route.getDescription()).thenReturn("A fun night out");
        when(route.getTotalDistanceKm()).thenReturn(5.2);
        when(route.getCreatedDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(route.getBars()).thenReturn(List.of(bar));
        when(route.getEstimatedTime()).thenReturn(120);
        return route;
    }

    /** Makes mockResultSet return exactly one route row, then no more rows. */
    private void stubSingleRouteRow(String locationsJson, String username) throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("route_name")).thenReturn("Route A");
        when(mockResultSet.getString("route_description")).thenReturn("Desc A");
        when(mockResultSet.getDate("created_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 6, 1)));
        when(mockResultSet.getString("route_locations")).thenReturn(locationsJson);
        when(mockResultSet.getString("username")).thenReturn(username);
    }

    @Test
    void saveRoute_success_returnsTrueAndSetsGeneratedId() throws SQLException {
        Route route = buildMockRoute();
        when(mockInsertStatement.executeUpdate()).thenReturn(1);
        when(mockInsertStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(42);

        boolean result = routeDAO.saveRoute(route, 7);

        assertTrue(result);
        verify(route).setRouteID(42);
        verify(mockInsertStatement).setInt(1, 7);
        verify(mockInsertStatement).setString(2, "Friday Night Crawl");
        verify(mockInsertStatement).setString(3, "A fun night out");
        verify(mockInsertStatement).setDouble(4, 5.2);
        verify(mockInsertStatement).setDate(5, Date.valueOf(LocalDate.of(2024, 1, 1)));
        verify(mockInsertStatement).setString(6, "[\"bar-1\"]");
        verify(mockInsertStatement).setInt(7, 120);
    }

    @Test
    void saveRoute_noRowsAffected_returnsFalseAndDoesNotSetId() throws SQLException {
        Route route = buildMockRoute();
        when(mockInsertStatement.executeUpdate()).thenReturn(0);

        boolean result = routeDAO.saveRoute(route, 7);

        assertFalse(result);
        verify(mockInsertStatement).executeUpdate();
        verify(route, never()).setRouteID(anyInt());
    }

    @Test
    void saveRoute_sqlException_throwsDataAccessException() throws SQLException {
        Route route = buildMockRoute();
        when(mockInsertStatement.executeUpdate()).thenThrow(new SQLException("Failed to save route"));

        assertThrows(DataAccessException.class, () -> routeDAO.saveRoute(route, 7));
    }

    @Test
    void saveRoute_duplicateName_returnsFalseWithoutInserting() throws SQLException {
        Route route = buildMockRoute();
        stubSingleRouteRow("[]", "alice");

        try (MockedConstruction<Route> ignored = mockConstruction(Route.class,
                (m, ctx) -> when(m.getName()).thenReturn("Friday Night Crawl"))) {
            assertFalse(routeDAO.saveRoute(route, 7));
        }

        verify(mockConnection, never()).prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockInsertStatement, never()).executeUpdate();
    }

    @Test
    void saveRoute_existingRouteWithDifferentName_stillInserts() throws SQLException {
        Route route = buildMockRoute();
        stubSingleRouteRow("[]", "alice");
        when(mockInsertStatement.executeUpdate()).thenReturn(1);
        when(mockInsertStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        boolean result;
        try (MockedConstruction<Route> ignored = mockConstruction(Route.class,
                (m, ctx) -> when(m.getName()).thenReturn("Some Other Route"))) {
            result = routeDAO.saveRoute(route, 7);
        }

        assertTrue(result);
        verify(mockInsertStatement).executeUpdate();
        verify(route, never()).setRouteID(anyInt());
    }


    @Test
    void publishCommunityRoute_success_returnsTrue() throws SQLException {
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = routeDAO.publishCommunityRoute(route, 7);

        assertTrue(result);
        verify(mockStatement).setInt(1, 7);
        verify(mockStatement).setString(2, "Friday Night Crawl");
    }

    @Test
    void publishCommunityRoute_noMatch_returnsFalse() throws SQLException {
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenReturn(0);

        assertFalse(routeDAO.publishCommunityRoute(route, 7));
    }

    @Test
    void publishCommunityRoute_sqlException_throwsDataAccessException() throws SQLException {
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("update failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.publishCommunityRoute(route, 7));
    }


    @Test
    void getUserRoutes_mapsResultSetRowIntoRouteConstructorArgs() throws SQLException {
        stubSingleRouteRow("[\"bar-1\",\"bar-2\"]", "alice");

        Bar bar1 = mock(Bar.class);
        Bar bar2 = mock(Bar.class);
        when(mockLocationManager.findById("bar-1")).thenReturn(bar1);
        when(mockLocationManager.findById("bar-2")).thenReturn(bar2);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> mockedRoute = mockConstruction(Route.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            List<Route> routes = routeDAO.getUserRoutes(5);

            assertEquals(1, routes.size());
            assertEquals(mockedRoute.constructed(), routes);
            assertEquals(1, capturedArgs.size());

            List<?> args = capturedArgs.get(0);
            assertEquals(1, args.get(0));
            assertEquals("Route A", args.get(1));
            assertEquals("Desc A", args.get(2));
            assertEquals(List.of(bar1, bar2), args.get(3));
            assertEquals(LocalDate.of(2024, 6, 1), args.get(4));
            assertEquals("alice", args.get(5));
        }
        verify(mockStatement).setInt(1, 5);
    }

    @Test
    void getUserRoutes_missingLocationId_isSkippedFromBarsPassedToRoute() throws SQLException {
        stubSingleRouteRow("[\"present-bar\",\"missing-bar\"]", "alice");

        Bar presentBar = mock(Bar.class);
        when(mockLocationManager.findById("present-bar")).thenReturn(presentBar);
        when(mockLocationManager.findById("missing-bar")).thenReturn(null);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> ignored = mockConstruction(Route.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            List<Route> routes = routeDAO.getUserRoutes(5);

            assertEquals(1, routes.size());
            assertEquals(List.of(presentBar), capturedArgs.get(0).get(3));
        }
    }

    @Test
    void getUserRoutes_nullLocationJson_passesEmptyBarList() throws SQLException {
        stubSingleRouteRow(null, "alice");

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> ignored = mockConstruction(Route.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            routeDAO.getUserRoutes(5);

            assertEquals(List.of(), capturedArgs.get(0).get(3));
        }
    }

    @Test
    void getUserRoutes_nullCreatedDate_passesNullDate() throws SQLException {
        stubSingleRouteRow("[]", "alice");
        when(mockResultSet.getDate("created_date")).thenReturn(null);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> ignored = mockConstruction(Route.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            routeDAO.getUserRoutes(5);

            assertEquals(null, capturedArgs.get(0).get(4));
        }
    }

    @Test
    void getUserRoutes_noRows_returnsEmptyList() {
        assertTrue(routeDAO.getUserRoutes(5).isEmpty());
    }

    @Test
    void getUserRoutes_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getUserRoutes(5));
    }

    @Test
    void getCommunityRoutes_noRows_returnsEmptyAndDoesNotBindAnIntParameter() throws SQLException {
        List<Route> routes = routeDAO.getCommunityRoutes();

        assertTrue(routes.isEmpty());
        verify(mockStatement, never()).setInt(anyInt(), anyInt());
    }

    @Test
    void getCommunityRoutes_withRow_mapsOwnerUsername() throws SQLException {
        stubSingleRouteRow("[]", "bob");

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> mockedRoute = mockConstruction(Route.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            List<Route> routes = routeDAO.getCommunityRoutes();

            assertEquals(1, routes.size());
            assertEquals(mockedRoute.constructed(), routes);
            assertEquals("bob", capturedArgs.get(0).get(5));
        }
        verify(mockStatement, never()).setInt(anyInt(), anyInt());
    }

    @Test
    void getCommunityRoutes_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getCommunityRoutes());
    }

    @Test
    void deleteSavedRoute_success_returnsTrue() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(1);

        assertTrue(routeDAO.deleteSavedRoute(3));
        verify(mockStatement).setInt(1, 3);
    }

    @Test
    void deleteSavedRoute_noMatch_returnsFalse() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(0);

        assertFalse(routeDAO.deleteSavedRoute(3));
    }

    @Test
    void deleteSavedRoute_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("delete failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.deleteSavedRoute(3));
    }


    @Test
    void getRouteVote_rowPresent_returnsCount() throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("count(vote)")).thenReturn(4);

        int result = routeDAO.getRouteVote(9, 1);

        assertEquals(4, result);
        verify(mockStatement).setInt(1, 9);
        verify(mockStatement).setInt(2, 1);
    }

    @Test
    void getRouteVote_noRow_returnsZero() {
        assertEquals(0, routeDAO.getRouteVote(9, 1));
    }

    @Test
    void getRouteVote_queryFails_throwsNoRouteRatings() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(NoRouteRatings.class, () -> routeDAO.getRouteVote(9, 1));
    }

    @Test
    void getRouteVote_prepareFails_throwsDataAccessException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getRouteVote(9, 1));
    }


    @Test
    void voteRoute_bindsParametersAndExecutesUpdate() throws SQLException {
        routeDAO.voteRoute(7, 9, 1);

        verify(mockStatement).setInt(1, 7);
        verify(mockStatement).setInt(2, 9);
        verify(mockStatement).setInt(3, 1);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void voteRoute_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("vote failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.voteRoute(7, 9, 1));
    }


    @Test
    void getUserVotes_returnsRouteIdsInOrder() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("route_id")).thenReturn(10, 20);

        List<Integer> result = routeDAO.getUserVotes(7, 1);

        assertEquals(List.of(10, 20), result);
        verify(mockStatement).setInt(1, 7);
        verify(mockStatement).setInt(2, 1);
    }

    @Test
    void getUserVotes_noRows_returnsEmptyList() {
        assertTrue(routeDAO.getUserVotes(7, 1).isEmpty());
    }

    @Test
    void getUserVotes_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getUserVotes(7, 1));
    }

    @Test
    void getRouteVotes_buildsRatingFromCountsAndUserVote() throws SQLException {
        PreparedStatement countStatement = mock(PreparedStatement.class);
        PreparedStatement userStatement = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        ResultSet userRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(countStatement, userStatement);
        when(countStatement.executeQuery()).thenReturn(countRs);
        when(userStatement.executeQuery()).thenReturn(userRs);

        when(countRs.next()).thenReturn(true, true, false);
        when(countRs.getInt("vote")).thenReturn(1, -1);
        when(countRs.getInt("vote_count")).thenReturn(5, 2);
        when(userRs.next()).thenReturn(true);
        when(userRs.getInt("vote")).thenReturn(-1);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<RouteRating> ignored = mockConstruction(RouteRating.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            routeDAO.getRouteVotes(9, 7);
        }

        assertEquals(List.of(5, 2, -1), capturedArgs.get(0));
        verify(countStatement).setInt(1, 9);
        verify(userStatement).setInt(1, 7);
        verify(userStatement).setInt(2, 9);
    }

    @Test
    void getRouteVotes_noVotes_defaultsToZeros() throws SQLException {
        PreparedStatement countStatement = mock(PreparedStatement.class);
        PreparedStatement userStatement = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        ResultSet userRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(countStatement, userStatement);
        when(countStatement.executeQuery()).thenReturn(countRs);
        when(userStatement.executeQuery()).thenReturn(userRs);
        when(countRs.next()).thenReturn(false);
        when(userRs.next()).thenReturn(false);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<RouteRating> ignored = mockConstruction(RouteRating.class,
                (m, context) -> capturedArgs.add(context.arguments()))) {

            routeDAO.getRouteVotes(9, 7);
        }

        assertEquals(List.of(0, 0, 0), capturedArgs.get(0));
    }

    @Test
    void getRouteVotes_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getRouteVotes(9, 7));
    }
}