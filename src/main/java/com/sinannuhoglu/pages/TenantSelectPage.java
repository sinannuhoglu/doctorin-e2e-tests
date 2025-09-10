package com.sinannuhoglu.pages;

import com.sinannuhoglu.core.BasePage;
import com.sinannuhoglu.core.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Tenant switch modal: aç, isim gir, kaydet. */
public class TenantSelectPage extends BasePage {

    public TenantSelectPage(WebDriver driver, ConfigReader cfg) { super(driver, cfg); }
    public TenantSelectPage(WebDriver driver) { super(driver); }

    private final By tenantSwitchId   = By.id("AppTenantSwitchLink");
    private final By tenantSwitchText = By.xpath("//*[self::a or self::button][normalize-space()='değiştir' or .//span[normalize-space()='değiştir']]");
    private final By tenantForm       = By.cssSelector("form[action*='TenantSwitchModal']");
    private final By tenantInput      = By.id("Input_Name");
    private final By saveButton       = By.cssSelector("form[action*='TenantSwitchModal'] button[type='submit']");

    /** Modalı açar ve giriş alanını bekler. */
    public TenantSelectPage openTenantModal() {
        try { clickWithFallback(tenantSwitchId); }
        catch (Exception ignore) { clickWithFallback(tenantSwitchText); }
        waitVisible(tenantInput);
        return this;
    }

    /** Tenant seçer ve modalın kapanmasını bekler. */
    public void selectTenant(String tenantName) {
        type(tenantInput, tenantName);
        clickWithFallback(saveButton);
        waitInvisible(tenantForm);
    }
}
