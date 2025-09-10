package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.AppointmentsPage;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import org.openqa.selenium.WebDriver;

/** Randevu slotu etkileşim adımları. */
public class AppointmentSlotSteps {

    private WebDriver driver;
    private AppointmentsPage appointments;

    @Before(order = 1)
    public void init() {
        driver = DriverManager.getDriver();
        appointments = new AppointmentsPage(driver);
    }

    @And("^I click the (\\d{1,2}):(\\d{2}) slot$")
    public void iClickTheSlot(Integer hour, Integer minute) {
        appointments.clickSlotAt(hour, minute);
    }

    @And("I search patient {string}")
    public void iSearchPatient(String fullName) {
        appointments.searchPatientInSidebar(fullName);
    }

    @And("I open the appointment details again")
    public void iOpenAppointmentDetailsAgain() {
        appointments.openAppointmentDetailsOfLastSlot();
    }

    @And("I delete the appointment of the last slot")
    public void iDeleteAppointmentOfTheLastSlot() {
        appointments.deleteAppointmentOfLastSlot();
    }
}
