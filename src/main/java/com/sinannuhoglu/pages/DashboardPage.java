package com.sinannuhoglu.pages;

import com.sinannuhoglu.core.BasePage;
import org.openqa.selenium.*;

/**
 * Dashboard: giriş sonrası ana panel.
 * Hızlı imzalarla yüklenmeyi doğrular, modül geçişlerini sağlar.
 */
public class DashboardPage extends BasePage {

    public DashboardPage(WebDriver driver) { super(driver); }

    // ---- Locators (yükleme imzaları) ----
    private final By doctorinBrand = By.xpath("//span[contains(normalize-space(),'Doctorin')]");
    private final By modulesHeader = By.xpath("//h2[contains(normalize-space(),'Modüller')]");
    private final By leftRail      = By.xpath("//nav[contains(@class,'h-full') and contains(@class,'w-[60px]')]");
    private final By anyPanel      = By.xpath("//div[contains(@class,'panel__wrapper-shadow-default')]");

    // ---- Modül linkleri ----
    private final By appointmentModuleByHref =
            By.cssSelector("a[href*='appointment-service/appointments']");

    private By moduleByTitle(String title) {
        return By.xpath("//a[.//p[contains(@class,'text-sm') and contains(@class,'truncate') and normalize-space()='"
                + title + "']]");
    }

    /** Login sonrasında dashboard imzalarının göründüğünü doğrular. */
    public void assertLoaded() {
        waitUntilUrlDoesNotContain("/account/login");
        waitAnyVisible(doctorinBrand, modulesHeader, leftRail, anyPanel);
    }

    /** Başlığı verilen modül kartına tıklar; bulunamazsa Randevular'a düşer. */
    public void openModuleByName(String moduleTitle) {
        try {
            clickWithFallback(moduleByTitle(moduleTitle));
        } catch (Exception ignored) {
            clickWithFallback(appointmentModuleByHref);
        }
    }

    /** Randevular modülünü açar ve sayfanın yüklendiğini doğrulayarak döner. */
    public AppointmentsPage openAppointments() {
        waitVisible(appointmentModuleByHref);
        clickWithFallback(appointmentModuleByHref);

        for (int i = 0; i < 20; i++) {
            if (driver.getCurrentUrl().contains("/appointment-service/appointments")) break;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return new AppointmentsPage(driver).assertLoaded();
    }
}
