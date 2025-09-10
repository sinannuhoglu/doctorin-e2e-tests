@smoke @appointment_e2e @resources
Feature: Definitions > Resources workplan operations

  Background:
    Given I am on the Doctorin login page
    And I switch tenant to "Nişantaşı Klinik"
    And I login with username "Test" and password "Test123."
    And I should land on the dashboard
    And I open the Appointments module
    And I should be on the appointments page
    And I open Definitions from the side panel
    And I open Resources under Definitions

  Scenario: Tanımlar > Kaynaklar > Düzenle > Çalışma Takvimi > Gün seç
    And I click Edit for resource "Prof. Dr. Turgut Aydin"
    And I open the Workplan tab in the resource editor
    And I open the workplan for day "Cuma"
    And I ensure the Workplan bar is visible
    And I set the workplan start time to "08:00"
    And I set the workplan end time to "18:00"
    And I select Branch as "Nişantaşı"
    And I select Appointment Type as "Hepsini Seç"
    And I select Platform as "Hepsini Seç"
    And I select Department as "Hepsini Seç"
    And I click the Save button in the Workplan modal
