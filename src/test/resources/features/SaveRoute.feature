Feature: Save a created route (UC_5)

  @AT_3
  Scenario: Save a route while being logged in
    Given The user is logged in
    And A route has been created on the home screen
    When The user selects Save Route
    Then The route is added to their saved routes
