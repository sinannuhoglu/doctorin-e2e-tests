@smoke @appointment_e2e
Feature: Workplan'dan randevu akışına tam uçtan uca (bağımsız çalışır)

  Background:
    Given I am on the Doctorin login page
    And I switch tenant to "Nişantaşı Klinik"
    And I login with username "Test" and password "Test123."
    And I should land on the dashboard
    And I open the Appointments module
    And I should be on the appointments page
    And I open Definitions from the side panel
    And I open Resources under Definitions

  Scenario: Çalışma takvimi güncelle, kaydet ve randevu ekranında slot aç/kapat
    And I click Edit for resource "Prof. Dr. Derman Bulur"
    And I open the Workplan tab in the resource editor
    And I open the workplan for day "Çarşamba"
    And I ensure the Workplan bar is visible
    And I set the workplan start time to "08:00"
    And I set the workplan end time to "18:00"
    And I select Branch as "Nişantaşı"
    And I select Appointment Type as "Hepsini Seç"
    And I select Platform as "Hepsini Seç"
    And I select Department as "Hepsini Seç"
    And I save the Workplan and return to Appointments

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
