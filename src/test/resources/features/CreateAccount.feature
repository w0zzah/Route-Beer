Feature: Creating an account (UC_4)

  @AT_7
  Scenario: New user creating an account
    Given The user is on the create account window
    And Has a valid username and password entered
    When The user selects 'Create Account'
    Then The account becomes registered in the database
    And The user is directed to the main window

  @AT_20
  Scenario: User attempts to create an account with a previously registered username
    Given The user is on the create account window
    And Has a username inputted which is already registered
    When The user selects 'Create Account'
    Then The user is notified that the username is already taken
