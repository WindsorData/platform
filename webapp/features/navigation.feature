Feature: Depending on the role, the user has to de redirected to a different page

  Scenario: A super user is redirected to users list
    Given I am a "super" user
    When I log in
    Then I should be in "/users" page

  Scenario: An admin user is redirected to file upload page
    Given I am a "admin" user
    When I log in
    Then I should be in "/files/single_file" page

  Scenario: A client user is redirected to users list
    Given I am a "client" user
    When I log in
    Then I should be in "/search/quick_search" page    