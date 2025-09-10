package com.sinannuhoglu.hooks;

import com.sinannuhoglu.core.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Cucumber hooks: initialize context before scenarios, attach screenshot on failure, then teardown. */
public class Hooks {

    @Before(order = 0)
    public void startUp() {
        TestContext.get().init();
    }

    @After(order = 100)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            attachScreenshot();
            System.setProperty("keepOpen", "true");
        }
        TestContext.get().quit();
    }

    @Attachment(value = "Failure screenshot", type = "image/png")
    private byte[] attachScreenshot() {
        try {
            return ((TakesScreenshot) TestContext.get().driver())
                    .getScreenshotAs(OutputType.BYTES);
        } catch (Throwable t) {
            return new byte[0];
        }
    }
}
