package com.sinannuhoglu.steps;

import com.sinannuhoglu.pages.AppointmentResourceWorkplanPage;
import com.sinannuhoglu.pages.AppointmentWorkplanBarPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

public class AppointmentResourceWorkplanSteps {

    private final WebDriver driver;
    private final AppointmentResourceWorkplanPage page;
    private final AppointmentWorkplanBarPage bar;

    public AppointmentResourceWorkplanSteps() {
        this.driver = com.sinannuhoglu.core.DriverManager.getDriver();
        this.page = new AppointmentResourceWorkplanPage(driver);
        this.bar = new AppointmentWorkplanBarPage(driver);
    }

    @And("I open the Workplan tab in the resource editor")
    public void iOpenWorkplanTab() { page.openWorkplanTab(); }

    @And("I open the workplan for day {string}")
    public void iOpenWorkplanForDay(String dayTr) { page.openWorkplanForDay(dayTr); }

    @And("I ensure the Workplan bar is visible")
    public void iEnsureWorkplanBarIsVisible() {
        page.waitForWorkplanManagementBar();
        bar.ensureVisible();
    }

    @Then("I should see the Workplan Management panel")
    public void iShouldSeeTheWorkplanManagementPanel() { bar.ensureVisible(); }

    @And("I set the workplan start time to {string}")
    public void iSetWorkplanStartTimeTo(String hhmm) {
        bar.ensureVisible();
        bar.setStartTime(hhmm);
    }

    @And("I set the workplan end time to {string}")
    public void iSetWorkplanEndTimeTo(String hhmm) {
        bar.ensureVisible();
        bar.setEndTime(hhmm);
    }

    @And("I select Branch as {string}")
    public void iSelectBranchAs(String branchName) {
        bar.ensureVisible();
        bar.selectBranch(branchName);
    }

    @And("I select Appointment Type as {string}")
    public void iSelectAppointmentTypeAs(String value) {
        bar.ensureVisible();
        if (equalsHepsiniSec(value)) {
            bar.selectAllAppointmentTypes();
        } else {
            bar.selectAppointmentTypesByTexts(value);
        }
    }

    @And("I select Department as {string}")
    public void iSelectDepartmentAs(String value) {
        bar.ensureVisible();
        if (equalsHepsiniSec(value)) {
            bar.selectAllDepartments();
        } else {
            bar.selectDepartmentsByTexts(value);
        }
    }

    @And("I select Platform as {string}")
    public void iSelectPlatformAs(String value) {
        bar.ensureVisible();
        bar.ensureAllPlatformsSelected();
    }

    @And("I click the Save button in the Workplan modal")
    public void iClickSaveInWorkplanModal() { bar.clickSave(); }

    @And("I save the Workplan and return to Appointments")
    public void iSaveWorkplanAndReturnToAppointments() { bar.clickSaveAndReturnToAppointments(); }

    private boolean equalsHepsiniSec(String v) {
        if (v == null) return false;
        String n = v.trim().toLowerCase();
        return n.equals("hepsini seç") || n.equals("hepsini sec") || n.equals("all");
    }
}
