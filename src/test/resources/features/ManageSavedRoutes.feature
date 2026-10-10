Feature: Manage saved routes (UC_6)

  @AT_5
  Scenario: View saved routes
    Given A user is signed in with one or more saved routes
    When The user selects 'Saved Routes'
    Then Each route associated with the account is shown

  @AT_6
  Scenario: Loading a saved a route
    Given A user is signed in
    And The user is on the 'Saved Routes' tab
    When The user selects the 'Load' button
    Then The route is generated onto the map
    And The route details are displayed in the 'Route Details' tab

  @AT_22
  Scenario: Deleting a saved route
    Given The user has a saved route in 'Saved Routes'
    When the user selects 'Delete' on the route card and confirms
    Then The route is removed from their saved routes and cannot be retrieved