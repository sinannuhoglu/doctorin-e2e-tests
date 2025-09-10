package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.DashboardPage;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

/** Dashboard navigasyon adımları. */
public class DashboardSteps {

    private final WebDriver driver = DriverManager.getDriver();
    private final DashboardPage dashboard = new DashboardPage(driver);

    @When("I open the Appointments module")
    public void iOpenAppointmentsModule() {
        dashboard.openAppointments();
    }

    @When("I open the {string} module")
    public void iOpenNamedModule(String moduleName) {
        dashboard.openModuleByName(moduleName);
    }
}
