package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.AppointmentsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AppointmentFilterSteps {

    private final AppointmentsPage page;

    public AppointmentFilterSteps() {
        this.page = new AppointmentsPage(DriverManager.getDriver());
    }

    @When("I open the filter panel")
    public void iOpenTheFilterPanel() {
        page.openFilterPanel();
    }

    @When("I choose branch {string} in filters")
    public void iChooseBranchInFilters(String branch) {
        page.selectBranch(branch);
    }

    @Then("branch filter should be {string}")
    public void branch_filter_should_be(String expectedBranch) {
        page.assertBranchValue(expectedBranch);
    }

    @When("I choose department {string} in filters")
    public void iChooseDepartmentInFilters(String dept) {
        page.selectDepartment(dept);
    }

    @When("I keep only doctor {string} in resources")
    public void iKeepOnlyDoctorInResources(String doctor) {
        page.keepOnlyDoctor(doctor);
    }

    @When("I apply the filters")
    public void iApplyTheFilters() {
        page.applyFilters();
    }
}
