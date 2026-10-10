package seng202.team5.gui.tab_pane_controllers;

import seng202.team5.models.Route;

/**
 * What the tab sub-controllers may ask of the control panel. Tabs never hold
 * references to each other; they go through this interface instead.
 */
public interface TabPaneCoordinator {

    /** Switches to the route builder tab. */
    void showRouteTab();

    /** Switches to the route details tab. */
    void showDetailsTab();

    /** Switches to the saved routes tab. */
    void showSavedTab();

    /** Loads and switches to the community routes tab. */
    void showCommunityTab();

    /** Returns to the sign-in / setup screen (confirming first if a route would be lost). */
    void returnToSetup();

    /**
     * Displays a newly generated route: details, map, blank name fields,
     * and the regenerate button.
     *
     * @param route the generated route
     */
    void showGeneratedRoute(Route route);

    /**
     * Opens a stored (saved or community) route in the details tab and on the map.
     *
     * @param route the route to open
     */
    void openRoute(Route route);

    /** Reloads the saved routes list. */
    void refreshSavedRoutes();

    /** Clears the map and details tab back to the "no route" state. */
    void resetRouteView();

    /** Re-runs route generation with the builder's current inputs. */
    void regenerateRoute();
}
