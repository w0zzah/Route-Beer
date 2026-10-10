Feature: Manage Current Account (UC_7)


  @AT_19
  Scenario: Log out of account
    Given The user is logged in with a saved route
    When The user selects 'Log Out'
    Then The user is directed back to the login page

  @AT_21
  Scenario: Delete existing account
    Given The user is logged in with a saved route
    When The user selects 'Delete Account' and confirms
    Then The user is taken back to the login page
    And All user associated data is deleted