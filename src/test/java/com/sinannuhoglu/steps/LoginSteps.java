package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.DashboardPage;
import com.sinannuhoglu.pages.LoginPage;
import com.sinannuhoglu.pages.TenantSelectPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

/** Login akışı adımları: tenant seçimi, giriş ve dashboard doğrulaması. */
public class LoginSteps {

    private final WebDriver driver = DriverManager.getDriver();

    @Given("I am on the Doctorin login page")
    public void openLoginPage() {
        new LoginPage(driver).open();
    }

    @When("I switch tenant to {string}")
    public void switchTenant(String tenantName) {
        new TenantSelectPage(driver)
                .openTenantModal()
                .selectTenant(tenantName);
    }

    @When("I login with username {string} and password {string}")
    public void loginWithCreds(String username, String password) {
        new LoginPage(driver)
                .fillCredentials(username, password)
                .submit();
    }

    @Then("I should land on the dashboard")
    public void shouldBeOnDashboard() {
        new DashboardPage(driver).assertLoaded();
    }
}
