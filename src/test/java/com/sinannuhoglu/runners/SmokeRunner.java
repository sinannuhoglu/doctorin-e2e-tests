package com.sinannuhoglu.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = {
                "src/test/resources/features/appointment/workplan_to_appointment_e2e.feature"
        },
        glue = {"com.sinannuhoglu.steps", "com.sinannuhoglu.hooks"},
        plugin = {"pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        monochrome = true
)
public class SmokeRunner extends AbstractTestNGCucumberTests { }
