Feature: Randevu oluşturma, tamamlama ve silme

  Background:
    Given I am on the Doctorin login page
    And I switch tenant to "Nişantaşı Klinik"
    And I login with username "Test" and password "Test123."
    And I should land on the dashboard
    And I open the Appointments module
    And I should be on the appointments page

  @smoke @appointment_e2e
  Scenario: Filtreleri uygula ve randevu slotunu aç
    When I open the filter panel
    And I choose branch "Nişantaşı" in filters
    Then branch filter should be "Nişantaşı"
    And I choose department "KBB" in filters
    And I keep only doctor "Prof. Dr. Derman Bulur" in resources
    And I apply the filters
    And I click the 10:30 slot
    And I search patient "HASTANUR İYİLEŞMEZ"
    And I open the appointment details again
    And I delete the appointment of the last slot
