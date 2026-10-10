Feature: Sign into an account (UC_14)

  @AT_8
  Scenario: User logging into a previously created account
    Given A user is on the login window
    When The user selects sign in with a registered username
    Then The user will load into the main window of the application
    And The data associated with the user will be loaded

  @AT_15 @AT_16
  Scenario: User enters an unregistered username on sign in
    Given A user is on the login window
    When The user selects sign in with an invalid username or password
    Then The user is notified that they have inputted an invalid username or password