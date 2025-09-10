package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.AppointmentsPage;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

/** Randevular sayfasına ilişkin doğrulama adımları. */
public class AppointmentsSteps {

    private final WebDriver driver = DriverManager.getDriver();

    /** Randevular sayfasının yüklendiğini doğrular. */
    @Then("I should be on the appointments page")
    public void shouldBeOnAppointmentsPage() {
        new AppointmentsPage(driver).assertLoaded();
    }
}
