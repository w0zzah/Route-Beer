Feature: UC_1 & UC_2 Generate a route.

  @AT1
  Scenario: Generate a route using minimal input values
    Given The user is on the main screen
    When The user selects generate route with a valid starting location
    Then A random number of bars between 3-9 are shown on the map interconnected with total walking distance
    And the route starts at the chosen starting location
    And the route total distance is under 10 km

  @AT2
  Scenario: Create a route while having filters applied.
    Given The user is on the main screen
    When the user selects Create Route with all the available fields filled out (start, end, distance, number of bars, themes, and features)
    Then a route is generated matching the specified start and end bars
    And the route has exactly 5 bars
    And the route's total distance is approximately 6.0 km
    And the route includes the requested features and themes

  @AT17
  Scenario Outline: Fail to generate a route due to impossible constraints
    Given The user is on the main screen
    When the user applies a strict <filter_type> filter that conflicts with the constraints
    Then a NoRouteGeneratedException is thrown with the message "<error_message>"

    Examples:
      | filter_type     | error_message                                                                            |
      | particular_bars | Couldn't fit all your required bars into a route this size.\nTry adjusting the filters.  |
      | themes          | Couldn't fit your selected theme(s) into a route.\nTry adjusting the filters.            |
      | features        | Couldn't fit your selected feature(s) into a route.\nTry adjusting the filters.          |
      | approx_distance | Couldn't get close to your target distance of 0.1km.\nTry adjusting the filters.         |