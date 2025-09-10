package com.sinannuhoglu.steps;

import com.sinannuhoglu.core.ConfigReader;
import com.sinannuhoglu.core.PageFactory;
import com.sinannuhoglu.core.TestContext;
import org.openqa.selenium.WebDriver;

/** Ortak adım tabanı: TestContext, WebDriver/ConfigReader erişimi ve sayfa oluşturma yardımcıları. */
public abstract class BaseSteps {

    protected final TestContext ctx = TestContext.get();

    protected WebDriver driver() { return ctx.driver(); }

    protected ConfigReader cfg() { return ctx.cfg(); }

    /** Page Object örneği oluşturur (PageFactory içindeki imza sıralamasını kullanır). */
    protected <T> T on(Class<T> pageClass) {
        return PageFactory.create(pageClass);
    }
}
