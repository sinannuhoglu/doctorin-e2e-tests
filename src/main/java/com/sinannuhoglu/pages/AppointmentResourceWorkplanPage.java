package com.sinannuhoglu.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

public class AppointmentResourceWorkplanPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    private final Actions actions;
    private static final Locale TR = Locale.forLanguageTag("tr");

    public AppointmentResourceWorkplanPage(WebDriver driver) {
        this.driver  = driver;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.js      = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }

    private WebElement dialogRoot() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".appointment-resources__dialog")));
    }

    /** “Çalışma Takvimi / Workplan” sekmesini açar. */
    public void openWorkplanTab() {
        WebElement dialog = dialogRoot();

        WebElement header = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(
                dialog, By.cssSelector(".e-tab-header")));

        List<WebElement> tabTexts = header.findElements(
                By.cssSelector(".e-toolbar-items .e-hscroll-content .e-toolbar-item .e-tab-text"));
        if (tabTexts.isEmpty()) {
            tabTexts = header.findElements(By.cssSelector(".e-toolbar-items .e-toolbar-item .e-tab-text"));
        }

        WebElement workplanTab = null;
        for (WebElement t : tabTexts) {
            String txt = (t.getText() == null ? "" : t.getText()).trim().toLowerCase(TR);
            if (txt.equals("çalışma takvimi") || txt.equals("workplan")) { workplanTab = t; break; }
        }
        if (workplanTab == null) {
            List<WebElement> byId = header.findElements(
                    By.cssSelector("[id^='e-item-tab-'][id$='_1'] .e-tab-text"));
            if (!byId.isEmpty()) workplanTab = byId.get(0);
        }
        if (workplanTab == null) {
            List<WebElement> byIndex = header.findElements(
                    By.cssSelector(".e-toolbar-item.e-template.e-ileft[data-index='1'] .e-tab-text"));
            if (!byIndex.isEmpty()) workplanTab = byIndex.get(0);
        }
        if (workplanTab == null) throw new NoSuchElementException("Sekme bulunamadı: Çalışma Takvimi/Workplan");

        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", workplanTab);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(workplanTab)).click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", workplanTab);
        }

        WebElement finalWorkplanTab = workplanTab;
        wait.until(d -> {
            WebElement wrap = finalWorkplanTab.findElement(By.xpath("./ancestor::div[contains(@class,'e-tab-wrap')]"));
            if ("true".equalsIgnoreCase(wrap.getAttribute("aria-selected"))) return true;
            WebElement item = finalWorkplanTab.findElement(By.xpath("./ancestor::div[contains(@class,'e-toolbar-item')]"));
            String cls = item.getAttribute("class");
            return cls != null && cls.contains("e-active");
        });
    }

    /** Haftalık görünümde verilen güne tıklar ve Workplan modalını bekler. */
    public void openWorkplanForDay(String dayTr) {
        WebElement dialog = dialogRoot();

        By contentBy = By.cssSelector(".e-content");
        wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(dialog, contentBy));

        By dayCellSel = By.cssSelector(
                ".e-content .e-table-container .e-content-wrap .e-schedule-table.e-content-table td.e-day-wrapper");
        List<WebElement> days = wait.until(drv -> {
            List<WebElement> list = dialog.findElements(dayCellSel);
            return list.size() >= 7 ? list : null;
        });

        int index = dayIndex(dayTr);
        WebElement dayCell = days.get(index);
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", dayCell);

        boolean clicked = false;
        List<WebElement> appts = dayCell.findElements(By.cssSelector(".e-appointment"));
        if (!appts.isEmpty()) {
            WebElement appt = appts.get(0);
            try {
                wait.until(ExpectedConditions.elementToBeClickable(appt)).click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", appt);
            }
            clicked = true;
        }

        if (!clicked) {
            List<WebElement> wrappers = dayCell.findElements(By.cssSelector("div[id^='e-appointment-wrapper-']"));
            if (!wrappers.isEmpty()) {
                WebElement wrapper = wrappers.get(0);
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(wrapper)).click();
                } catch (Exception e) {
                    js.executeScript("arguments[0].click();", wrapper);
                }
                clicked = true;
            }
        }

        if (!clicked) {
            try {
                new Actions(driver).moveToElement(dayCell,
                        Math.max(5, dayCell.getSize().width / 2),
                        Math.max(5, dayCell.getSize().height / 2)).click().perform();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", dayCell);
            }
        }

        waitForWorkplanManagementBar();
    }

    public boolean isWorkplanManagementBarVisible() {
        try { waitForWorkplanManagementBar(); return true; }
        catch (TimeoutException ignored) { return false; }
    }

    private WebElement findWorkplanModalInBody() {
        By modalsBy = By.cssSelector("div[id^='modal-dialog-'].e-dlg-container");
        List<WebElement> candidates = driver.findElements(modalsBy);
        for (WebElement m : candidates) {
            if (!m.isDisplayed()) continue;

            List<WebElement> headers = m.findElements(By.cssSelector(".e-dlg-header"));
            if (!headers.isEmpty()) {
                String h = (headers.get(0).getText() == null ? "" : headers.get(0).getText()).toLowerCase(TR);
                if (h.contains("takvim planı yönetimi") || h.contains("workplan")) {
                    return m;
                }
            }

            List<WebElement> grids = m.findElements(By.cssSelector(".grid.grid-cols-2.gap-3"));
            if (!grids.isEmpty()) {
                List<WebElement> groups = m.findElements(By.cssSelector(".e-form-group.e-label-position-top"));
                if (groups.size() >= 6) return m;
            }
        }
        return null;
    }

    /** Workplan modalının görünür olmasını bekler. */
    public void waitForWorkplanManagementBar() {
        WebElement modal = wait.until((ExpectedCondition<WebElement>) d -> findWorkplanModalInBody());
        List<WebElement> header = modal.findElements(By.cssSelector(".e-dlg-header"));
        if (!header.isEmpty()) {
            wait.until(ExpectedConditions.visibilityOf(header.get(0)));
        } else {
            wait.until(ExpectedConditions.visibilityOf(
                    modal.findElement(By.cssSelector(".grid.grid-cols-2.gap-3"))));
            wait.until((ExpectedCondition<Boolean>) d ->
                    modal.findElements(By.cssSelector(".e-form-group.e-label-position-top")).size() >= 6);
        }
    }

    private int dayIndex(String dayTr) {
        String d = dayTr == null ? "" : dayTr.trim().toLowerCase(TR);
        Map<String,Integer> map = new HashMap<>();
        map.put("pazartesi", 0);
        map.put("salı", 1); map.put("sali", 1);
        map.put("çarşamba", 2); map.put("carsamba", 2);
        map.put("perşembe", 3); map.put("persembe", 3);
        map.put("cuma", 4);
        map.put("cumartesi", 5);
        map.put("pazar", 6);
        Integer idx = map.get(d);
        if (idx == null) throw new IllegalArgumentException("Geçersiz gün: " + dayTr);
        return idx;
    }
}
