package seng202.team5.unittests.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import seng202.team5.data.DatabaseConnection;
import seng202.team5.data.RouteDAO;
import seng202.team5.exceptions.DataAccessException;
import seng202.team5.models.Bar;
import seng202.team5.models.Route;
import seng202.team5.services.LocationManager;
import seng202.team5.services.MainManager;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private ResultSet mockGeneratedKeys;

    private MockedStatic<DatabaseConnection> mockedDbConnection;

    private MainManager mockMainManager;
    private LocationManager mockLocationManager;

    // The non-static instance we are testing
    private RouteDAO routeDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockGeneratedKeys = mock(ResultSet.class);

        mockedDbConnection = mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        mockMainManager = mock(MainManager.class);
        mockLocationManager = mock(LocationManager.class);
        injectLocationManager(mockMainManager, mockLocationManager);

        // Initialize the DAO with injected dependencies
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

    @Test
    void saveRoute_success_returnsTrueAndSetsGeneratedId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(42);

        boolean result = routeDAO.saveRoute(route, 7);

        assertTrue(result);
        verify(route).setRouteID(42);
        verify(mockStatement).setInt(1, 7);
        verify(mockStatement).setString(2, "Friday Night Crawl");
        verify(mockStatement).setDouble(4, 5.2);
        verify(mockStatement).setInt(7, 120);
    }

    @Test
    void saveRoute_noRowsAffected_returnsFalseAndDoesNotSetId() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = routeDAO.saveRoute(route, 7);

        assertFalse(result);
        verify(route, never()).setRouteID(anyInt());
    }

    @Test
    void saveRoute_sqlException_throwsDataAccessException() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("insert failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.saveRoute(route, 7));
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

        boolean result = routeDAO.publishCommunityRoute(route, 7);

        assertFalse(result);
    }

    @Test
    void publishCommunityRoute_sqlException_throwsDataAccessException() throws SQLException {
        Route route = buildMockRoute();
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("update failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.publishCommunityRoute(route, 7));
    }

    @Test
    void getUserRoutes_mapsResultSetRowIntoRouteConstructorArgs() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("route_name")).thenReturn("Route A");
        when(mockResultSet.getString("route_description")).thenReturn("Desc A");
        when(mockResultSet.getDouble("total_distance")).thenReturn(3.5);
        when(mockResultSet.getDate("created_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 6, 1)));
        when(mockResultSet.getString("route_locations")).thenReturn("[\"bar-1\",\"bar-2\"]");
        when(mockResultSet.getBoolean("is_community")).thenReturn(false);

        Bar bar1 = mock(Bar.class);
        Bar bar2 = mock(Bar.class);
        when(mockLocationManager.findById("bar-1")).thenReturn(bar1);
        when(mockLocationManager.findById("bar-2")).thenReturn(bar2);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> mockedRoute = mockConstruction(Route.class,
                (mock, context) -> capturedArgs.add(context.arguments()))) {

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
        }
        verify(mockStatement).setInt(1, 5);
    }

    @Test
    void getUserRoutes_missingLocationId_isSkippedFromBarsPassedToRoute() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("route_name")).thenReturn("Route A");
        when(mockResultSet.getString("route_description")).thenReturn("Desc A");
        when(mockResultSet.getDouble("total_distance")).thenReturn(3.5);
        when(mockResultSet.getDate("created_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 6, 1)));
        when(mockResultSet.getString("route_locations")).thenReturn("[\"present-bar\",\"missing-bar\"]");
        when(mockResultSet.getBoolean("is_community")).thenReturn(false);

        Bar presentBar = mock(Bar.class);
        when(mockLocationManager.findById("present-bar")).thenReturn(presentBar);
        when(mockLocationManager.findById("missing-bar")).thenReturn(null);

        List<List<?>> capturedArgs = new ArrayList<>();
        try (MockedConstruction<Route> mockedRoute = mockConstruction(Route.class,
                (mock, context) -> capturedArgs.add(context.arguments()))) {

            List<Route> routes = routeDAO.getUserRoutes(5);

            assertEquals(1, routes.size());
            assertEquals(List.of(presentBar), capturedArgs.get(0).get(3));
        }
    }

    @Test
    void getUserRoutes_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("query failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.getUserRoutes(5));
    }

    @Test
    void getCommunityRoutes_doesNotBindAnIntParameter() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Route> routes = routeDAO.getCommunityRoutes();

        assertTrue(routes.isEmpty());
        verify(mockStatement, never()).setInt(anyInt(), anyInt());
    }

    @Test
    void deleteSavedRoute_success_returnsTrue() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = routeDAO.deleteSavedRoute(3);

        assertTrue(result);
        verify(mockStatement).setInt(1, 3);
    }

    @Test
    void deleteSavedRoute_noMatch_returnsFalse() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = routeDAO.deleteSavedRoute(3);

        assertFalse(result);
    }

    @Test
    void deleteSavedRoute_sqlException_throwsDataAccessException() throws SQLException {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("delete failed"));

        assertThrows(DataAccessException.class, () -> routeDAO.deleteSavedRoute(3));
    }
}