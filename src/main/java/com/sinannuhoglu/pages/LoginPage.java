package com.sinannuhoglu.pages;

import com.sinannuhoglu.core.BasePage;
import com.sinannuhoglu.util.AppConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/** LoginPage — hızlı açılış, iframe duyarlı, forma bağımlı olmayan submit. */
public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) { super(driver, 10); }

    // Locators (esnek/çoğul)
    private final By usernameAny = By.cssSelector(
            "input[name='LoginInput.UserNameOrEmailAddress'], #LoginInput_UserNameOrEmailAddress, input[name='username'], #i0116");

    private final By passwordAny = By.cssSelector(
            "input[type='password'], input[name='LoginInput.Password'], #password-input, #i0118");

    private final By submitAny = By.xpath(
            "//*[self::button or self::a][@type='submit' " +
                    "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ','abcdefghijklmnopqrstuvwxyzçğıöşü'),'giriş') " +
                    "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') " +
                    "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]");

    private final By anyForm = By.cssSelector("form");

    /** Sayfayı açar, DOM etkileşime hazır olunca login UI’ını garanti eder. */
    public LoginPage open() {
        driver.navigate().to(AppConfig.baseUrl());
        waitDomInteractive(8);
        if (!ensureLoginUi(10)) {
            hardStopLoading();
            ensureLoginUi(8);
        }
        return this;
    }

    /** Kullanıcı adı ve şifreyi, gerekiyorsa iframe içine geçerek yazar. */
    public LoginPage fillCredentials(String user, String pass) {
        ensureLoginContext();
        type(usernameAny, user, 10);
        type(passwordAny, pass, 10);
        return this;
    }

    /** Güvenli submit: Enter → buton → form.submit(); ardından login sonrası bekleme. */
    public void submit() {
        try {
            WebElement pwd = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.presenceOfElementLocated(passwordAny));
            pwd.sendKeys(Keys.ENTER);
            if (!isLikelyStillOnLogin()) {
                switchToDefault();
                waitAfterLogin();
                return;
            }
        } catch (Exception ignored) {}

        List<WebElement> btns = driver.findElements(submitAny);
        for (WebElement b : btns) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(b));
                try { b.click(); }
                catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click()", b); }
                break;
            } catch (StaleElementReferenceException | TimeoutException ignored) {}
        }

        if (isLikelyStillOnLogin()) {
            List<WebElement> forms = driver.findElements(anyForm);
            if (!forms.isEmpty()) {
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", forms.get(0)); }
                catch (Exception ignored) {}
            }
        }

        switchToDefault();
        waitAfterLogin();
    }

    // --- Helpers ---

    private void waitDomInteractive(int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(d -> {
                    try {
                        Object rs = ((JavascriptExecutor) d).executeScript("return document.readyState");
                        return "interactive".equals(rs) || "complete".equals(rs);
                    } catch (Exception e) { return false; }
                });
    }

    private void hardStopLoading() {
        try { ((JavascriptExecutor) driver).executeScript("window.stop();"); } catch (Exception ignored) {}
    }

    private boolean ensureLoginUi(int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            if (present(usernameAny, 1) || present(passwordAny, 1)) return true;
            if (switchToFrameWithLoginFields()) return true;
            sleep(200);
        }
        return present(usernameAny, 1) || present(passwordAny, 1);
    }

    private boolean switchToFrameWithLoginFields() {
        switchToDefault();
        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (WebElement frame : frames) {
            try {
                switchToDefault();
                driver.switchTo().frame(frame);
                if (!driver.findElements(passwordAny).isEmpty() || !driver.findElements(usernameAny).isEmpty()) {
                    return true;
                }
            } catch (NoSuchFrameException | StaleElementReferenceException ignored) {}
        }
        switchToDefault();
        return false;
    }

    private void ensureLoginContext() {
        if (present(usernameAny, 1) || present(passwordAny, 1)) return;
        if (!switchToFrameWithLoginFields()) {
            throw new NoSuchElementException("Login alanları bulunamadı (ana sayfa ve iframe'ler tarandı).");
        }
    }

    private void switchToDefault() {
        try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
    }

    private boolean isLikelyStillOnLogin() {
        try {
            String url = driver.getCurrentUrl();
            if (url != null && url.toLowerCase().contains("/account/login")) return true;
        } catch (Exception ignored) {}
        return !driver.findElements(passwordAny).isEmpty() || !driver.findElements(usernameAny).isEmpty();
    }

    private void waitAfterLogin() {
        final String loginPath = "/account/login";
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            try {
                String url = d.getCurrentUrl();
                if (url != null && !url.contains(loginPath)) return true;
                boolean modulesHeader = !d.findElements(By.xpath("//h2[contains(normalize-space(),'Modüller')]")).isEmpty();
                boolean leftRail      = !d.findElements(By.xpath("//nav[contains(@class,'h-full') and contains(@class,'w-[60px]')]")).isEmpty();
                boolean anyPanel      = !d.findElements(By.xpath("//div[contains(@class,'panel__wrapper-shadow-default')]")).isEmpty();
                return modulesHeader || leftRail || anyPanel;
            } catch (Exception ignore) { return false; }
        });
    }

    private boolean present(By locator, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) { return false; }
    }

    private void type(By locator, String text, int seconds) {
        WebElement el = new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
        try { el.clear(); } catch (Exception ignored) {}
        el.sendKeys(text);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
