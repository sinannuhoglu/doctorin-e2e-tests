Feature: Login

  @smoke @login
  Scenario: Select tenant and login with valid Test user
    Given I am on the Doctorin login page
    When I switch tenant to "Nişantaşı Klinik"
    And I login with username "Test" and password "Test123."
    Then I should land on the dashboard
    And I open the Appointments module
    Then I should be on the appointments page
