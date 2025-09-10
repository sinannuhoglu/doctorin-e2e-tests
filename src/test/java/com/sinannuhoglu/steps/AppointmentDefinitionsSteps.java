package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.DriverManager;
import com.sinannuhoglu.pages.AppointmentDefinitionsPage;
import io.cucumber.java.en.And;

public class AppointmentDefinitionsSteps {

    private final AppointmentDefinitionsPage defs;

    public AppointmentDefinitionsSteps() {
        this.defs = new AppointmentDefinitionsPage(DriverManager.getDriver());
    }

    @And("I open Definitions from the side panel")
    public void iOpenDefinitionsFromSidePanel() {
        defs.openDefinitionsFromSidePanel();
    }

    @And("I open Resources under Definitions")
    public void iOpenResourcesUnderDefinitions() {
        defs.openResourcesUnderDefinitions();
    }

    @And("I click Edit for resource {string}")
    public void iClickEditForResource(String resourceName) {
        defs.clickEditForResourceByName(resourceName);
    }
}
