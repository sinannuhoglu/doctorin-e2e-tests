package com.sinannuhoglu.core;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/*
 * BasePage
 * ------------------------------------------------------------------
 * Page Object’ler için ortak altyapı:
 *  - Explicit wait yardımcıları (visible/clickable/invisible, URL).
 *  - Güvenli etkileşimler (click fallback, jsClick, scrollIntoView).
 *  - Yüklenme kontrolleri (readyState: interactive/complete).
 *
 * Zaman aşımı
 *  - Varsayılan 20 sn (ctor ile override edilebilir).
 *  - ConfigReader varsa timeoutSec=cfg.getLong("timeoutSec", 20).
 */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final long timeoutSec;
    protected final WebDriverWait wait;

    private static long resolveTimeout(ConfigReader cfg, long fallback) {
        try {
            if (cfg != null) return cfg.getLong("timeoutSec", fallback);
        } catch (Throwable ignored) {}
        return fallback;
    }

    protected BasePage(WebDriver driver) { this(driver, 20L); }

    protected BasePage(WebDriver driver, long timeoutSec) {
        this.driver = driver;
        this.timeoutSec = timeoutSec;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(this.timeoutSec));
    }

    protected BasePage(WebDriver driver, ConfigReader cfg) { this(driver, resolveTimeout(cfg, 20L)); }

    // ---- Waits ----
    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void waitInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitInvisible(WebElement el) {
        wait.until(ExpectedConditions.invisibilityOf(el));
    }

    protected void waitUntilUrlContains(String token) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.urlContains(token));
    }

    protected void waitUntilUrlContains(String token, int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.urlContains(token));
    }

    protected void waitUntilUrlDoesNotContain(String token) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(d -> !d.getCurrentUrl().toLowerCase().contains(token.toLowerCase()));
    }

    /** document.readyState 'interactive' veya 'complete' olana kadar bekler. */
    protected void waitForInteractiveOrReady(long seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(d -> {
                    String rs = String.valueOf(((JavascriptExecutor) d).executeScript("return document.readyState"));
                    return "interactive".equals(rs) || "complete".equals(rs);
                });
    }

    /** document.readyState 'complete' olana kadar bekler. */
    protected void waitForDocumentReady() {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /** Verilen locator dizisinden ilk görünen elementi döndürür. */
    protected WebElement waitAnyVisible(By... locators) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(d -> {
                    for (By by : locators) {
                        try {
                            WebElement el = d.findElement(by);
                            if (el.isDisplayed()) return el;
                        } catch (NoSuchElementException ignored) {}
                    }
                    return null;
                });
    }

    // ---- Interactions ----
    protected void click(By locator) { waitClickable(locator).click(); }

    protected void clearAndType(By locator, String text) { type(locator, text); }

    protected void type(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    protected void jsClick(By locator) {
        WebElement el = waitVisible(locator);
        jsClick(el);
    }

    protected void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    /**
     * Güvenli tıklama zinciri:
     * 1) normal click → 2) scroll+JS click → 3) Actions click
     */
    protected void clickWithFallback(By locator) {
        try {
            waitClickable(locator).click();
        } catch (Exception e) {
            try {
                WebElement el = waitVisible(locator);
                scrollIntoView(el);
                jsClick(el);
            } catch (Exception ignored) {
                WebElement el = waitVisible(locator);
                new org.openqa.selenium.interactions.Actions(driver)
                        .moveToElement(el).click().perform();
            }
        }
    }

    // ---- Presence checks ----
    protected boolean isPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isPresent(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
