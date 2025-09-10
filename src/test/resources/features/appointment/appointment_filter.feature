@filters @smoke @appointment_e2e
Feature: Appointment filters

  Background:
    Given I am on the Doctorin login page
    And I switch tenant to "Nişantaşı Klinik"
    And I login with username "Test" and password "Test123."
    And I should land on the dashboard
    And I open the Appointments module
    And I should be on the appointments page

  Scenario: Filter by Branch, Department and keep only one Doctor
    When I open the filter panel
    And I choose branch "Nişantaşı" in filters
    Then branch filter should be "Nişantaşı"
    And I choose department "Dahiliye" in filters
    And I keep only doctor "Prof. Dr. Derman Bulur" in resources
    And I apply the filters
