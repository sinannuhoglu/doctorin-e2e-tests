package com.sinannuhoglu.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Takvim Planı Yönetimi (Workplan) modal bar'ı için Page Object.
 * Syncfusion dropdown/multiselect popup'ları body altında render ettiği için
 * stale/aşınma durumlarına toleranslı tasarlanmıştır.
 */
public class AppointmentWorkplanBarPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final FluentWait<WebDriver> waitIgnoringStale;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Duration SHORT   = Duration.ofSeconds(8);
    private static final Locale   TR      = Locale.forLanguageTag("tr");

    private WebElement modalRoot;

    public AppointmentWorkplanBarPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
        this.waitIgnoringStale = new WebDriverWait(driver, TIMEOUT)
                .ignoring(StaleElementReferenceException.class);
    }

    /* ============================================================
                             GENEL
       ============================================================ */

    private boolean isStale(WebElement el) {
        if (el == null) return true;
        try { el.getTagName(); return false; }
        catch (StaleElementReferenceException e) { return true; }
    }

    private WebElement freshModalRoot() {
        if (isStale(modalRoot)) modalRoot = null;
        if (modalRoot != null && modalRoot.isDisplayed()) return modalRoot;

        By header = By.xpath(
                "//div[contains(@class,'e-dlg-header')]//*[contains(normalize-space(.),'Takvim Planı Yönetimi') or contains(normalize-space(.),'Workplan Management')]"
        );
        try {
            WebElement h = waitIgnoringStale.until(ExpectedConditions.visibilityOfElementLocated(header));
            modalRoot = h.findElement(By.xpath("./ancestor::div[contains(@class,'e-dlg-container')][1]"));
            return modalRoot;
        } catch (TimeoutException ignore) { }

        List<WebElement> visibles = driver.findElements(By.cssSelector("div.e-dlg-container"))
                .stream().filter(el -> {
                    try { return el.isDisplayed(); } catch (Exception e) { return false; }
                }).collect(Collectors.toList());
        if (visibles.isEmpty()) throw new NoSuchElementException("Görünür modal (e-dlg-container) bulunamadı.");
        modalRoot = visibles.get(visibles.size() - 1);
        return modalRoot;
    }

    private WebElement dlgContent() {
        WebElement root = freshModalRoot();
        return waitIgnoringStale.until(d -> root.findElement(By.cssSelector(".e-dlg-content")));
    }

    private WebElement groupByLabel(String trLabel) {
        String xp = ".//div[contains(@class,'e-form-group') and contains(@class,'e-label-position-top')]" +
                "[.//label[contains(normalize-space(.),'" + trLabel + "')]]";
        return waitIgnoringStale.until(d -> dlgContent().findElement(By.xpath(xp)));
    }

    /** Workplan bar görünür ve yüklenmiş mi? */
    public void ensureVisible() {
        waitIgnoringStale.until(d -> freshModalRoot().isDisplayed());
        By groups = By.cssSelector(".e-dlg-content .e-form-group.e-label-position-top");
        By start  = By.cssSelector("input#work-schedule-start-time");
        By end    = By.cssSelector("input#work-schedule-end-time");

        waitIgnoringStale.until(d -> {
            try {
                WebElement root = freshModalRoot();
                if (!root.isDisplayed()) return false;
                if (dlgContent().findElements(groups).size() >= 1) return true;
                WebElement s = dlgContent().findElement(start);
                WebElement e = dlgContent().findElement(end);
                return s.isDisplayed() && e.isDisplayed();
            } catch (NoSuchElementException | StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    private void safeClick(WebElement el) {
        try {
            waitIgnoringStale.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", el);
        }
    }

    private void closePopupWithEsc() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        try { Thread.sleep(120); } catch (InterruptedException ignored) {}
    }

    /** En alttaki açık Syncfusion popup'ı (dropdown/multiselect) */
    private WebElement getBottomMostOpenPopup() {
        List<By> tries = List.of(
                By.cssSelector("div.e-ddl.e-control.e-lib.e-popup.e-popup-open[role='dialog']"),
                By.cssSelector("div.e-dropdownbase.e-popup-open[role='dialog']"),
                By.cssSelector("div.e-ddl.e-multi-select-list-wrapper.e-popup-open[role='dialog']"),
                By.cssSelector("div.e-ddl.e-popup-open[role='dialog']")
        );
        for (By sel : tries) {
            List<WebElement> pops = driver.findElements(sel).stream()
                    .filter(p -> {
                        try { return p.isDisplayed(); } catch (Exception e) { return false; }
                    }).collect(Collectors.toList());
            if (!pops.isEmpty()) return pops.get(pops.size() - 1);
        }
        throw new NoSuchElementException("Açık dropdown/multiselect popup bulunamadı.");
    }

    /** tr-aware normalize (küçült, boşluk sadele, diakritik kaldır) */
    private String norm(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(TR).replaceAll("\\s+", " ").trim();
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    private <T> T retryOnStale(Supplier<T> supplier) {
        RuntimeException last = null;
        for (int i = 0; i < 3; i++) {
            try { return supplier.get(); }
            catch (StaleElementReferenceException e) {
                last = e;
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }
        if (last != null) throw last;
        return supplier.get();
    }
    private void retryOnStale(Runnable r) { retryOnStale(() -> { r.run(); return true; }); }

    /* ============================================================
                         SAAT ALANLARI
       ============================================================ */

    public void setStartTime(String hhmm) {
        ensureVisible();
        retryOnStale(() -> {
            WebElement input;
            List<WebElement> byId = dlgContent().findElements(By.cssSelector("input#work-schedule-start-time"));
            input = byId.isEmpty() ? groupByLabel("Başlangıç Saati").findElement(By.cssSelector("input")) : byId.get(0);
            input.clear();
            input.sendKeys(hhmm);
            input.sendKeys(Keys.TAB);
            return true;
        });
    }

    public void setEndTime(String hhmm) {
        ensureVisible();
        retryOnStale(() -> {
            WebElement input;
            List<WebElement> byId = dlgContent().findElements(By.cssSelector("input#work-schedule-end-time"));
            input = byId.isEmpty() ? groupByLabel("Bitiş Saati").findElement(By.cssSelector("input")) : byId.get(0);
            input.clear();
            input.sendKeys(hhmm);
            input.sendKeys(Keys.TAB);
            return true;
        });
    }

    /* ============================================================
                       ŞUBE (SINGLE-SELECT DDL)
       ============================================================ */

    private static class DdlParts {
        final WebElement group;     // "Şube" alan grubu
        final WebElement wrapper;   // role=combobox
        final WebElement input;     // readonly text input
        final WebElement icon;      // ok ikonu
        DdlParts(WebElement g, WebElement w, WebElement i, WebElement ic){ group=g; wrapper=w; input=i; icon=ic; }
    }

    private DdlParts getBranchDdlParts() {
        WebElement grp = groupByLabel("Şube");
        WebElement wrapper = grp.findElement(By.cssSelector("[role='combobox']"));
        WebElement input   = grp.findElement(By.cssSelector("input.e-dropdownlist, input[readonly]"));
        List<WebElement> icons = grp.findElements(By.cssSelector(".e-input-group-icon.e-ddl-icon, .e-input-group-icon"));
        WebElement icon = icons.isEmpty() ? wrapper : icons.get(0);
        return new DdlParts(grp, wrapper, input, icon);
    }

    private WebElement waitPopupForWrapper(WebElement wrapper) {
        // aria-owns -> popup id
        String owns = wrapper.getAttribute("aria-owns"); // ör: work-schedule-facility_popup
        if (owns != null && !owns.isBlank()) {
            By open = By.cssSelector("div#" + owns + ".e-popup-open");
            try {
                return new WebDriverWait(driver, TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(open));
            } catch (TimeoutException ignore) { /* fallback'e düş */ }
            List<WebElement> any = driver.findElements(By.cssSelector("div#" + owns));
            for (WebElement el : any) if (el.isDisplayed()) return el;
        }
        return getBottomMostOpenPopup();
    }

    private String getInputValue(WebElement input) {
        String v = input.getAttribute("value");
        return v == null ? input.getText() : v;
    }

    /** Şube'yi gerçekten listeden seçer ve input değerini doğrular. */
    public void selectBranch(String branchName) {
        ensureVisible();

        DdlParts ddl = retryOnStale(this::getBranchDdlParts);

        retryOnStale(() -> { safeClick(ddl.icon); return true; });
        WebDriverWait w = new WebDriverWait(driver, TIMEOUT);
        WebElement popup;
        try {
            w.until(ExpectedConditions.attributeToBe(ddl.wrapper, "aria-expanded", "true"));
            popup = waitPopupForWrapper(ddl.wrapper);
        } catch (TimeoutException e) {
            new Actions(driver).moveToElement(ddl.wrapper)
                    .click().keyDown(Keys.ALT).sendKeys(Keys.ARROW_DOWN).keyUp(Keys.ALT).perform();
            popup = waitPopupForWrapper(ddl.wrapper);
        }

        List<WebElement> searchInputs = popup.findElements(By.cssSelector("input[type='text']"));
        if (!searchInputs.isEmpty()) {
            WebElement s = searchInputs.get(0);
            try { s.clear(); } catch (Exception ignore) {}
            s.sendKeys(branchName);
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }

        WebElement item = findListItemByText(popup, branchName);
        if (item == null) item = scrollUntilItem(popup, branchName, 24);
        if (item == null) throw new NoSuchElementException("Şube listesinde bulunamadı: " + branchName);

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'nearest'});", item);
        safeClick(item);

        try { new WebDriverWait(driver, SHORT).until(ExpectedConditions.invisibilityOf(popup)); } catch (Exception ignore) {}
        String want = norm(branchName);
        w.until(d -> norm(getInputValue(ddl.input)).equals(want));
    }

    private WebElement findListItemByText(WebElement popup, String expected) {
        String want = norm(expected);

        for (WebElement li : popup.findElements(By.cssSelector("li"))) {
            String t = norm(li.getText());
            if (t.equals(want)) return li;
            String dv = li.getAttribute("data-value");
            if (dv != null && norm(dv).equals(want)) return li;
            for (WebElement s : li.findElements(By.cssSelector("span,div"))) {
                if (norm(s.getText()).equals(want)) return li;
            }
        }
        for (WebElement d : popup.findElements(By.cssSelector("div.e-list-item"))) {
            if (norm(d.getText()).equals(want)) return d;
        }
        return null;
    }

    private WebElement scrollUntilItem(WebElement popup, String expected, int maxSteps) {
        WebElement scroll = null;
        for (By by : List.of(
                By.cssSelector(".e-content"),
                By.cssSelector("ul.e-list-parent"),
                By.cssSelector("div[style*='overflow']")
        )) {
            List<WebElement> found = popup.findElements(by);
            if (!found.isEmpty()) { scroll = found.get(0); break; }
        }
        if (scroll == null) scroll = popup;

        JavascriptExecutor js = (JavascriptExecutor) driver;
        long prev = -1;
        for (int i = 0; i < maxSteps; i++) {
            WebElement item = findListItemByText(popup, expected);
            if (item != null) return item;

            js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + 300;", scroll);

            Long cur = (Long) js.executeScript("return arguments[0].scrollTop;", scroll);
            if (cur != null && cur == prev) break;
            prev = cur == null ? prev : cur;

            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
        }
        return findListItemByText(popup, expected);
    }

    /* ============================================================
                 MULTI-SELECT: Randevu Tipi / Platform / Departman
       ============================================================ */

    private WebElement openMultiSelectAndGetPopup(String labelText) {
        ensureVisible();
        retryOnStale(() -> {
            WebElement grp = groupByLabel(labelText);
            WebElement trigger = grp.findElement(By.xpath(
                    ".//*[contains(@class,'e-multiselect') or contains(@class,'e-input-group') or self::input]"
            ));
            safeClick(trigger);
            return true;
        });
        return waitIgnoringStale.until(d -> getBottomMostOpenPopup());
    }

    private boolean popupShowsClearAll(WebElement popup) {
        String n = norm(popup.getText());
        return n.contains("tunun secimini kaldir") || n.contains("tümünün seçimini kaldır") || n.contains("clear all");
    }

    private WebElement findSelectAllElement(WebElement popup) {
        for (By sel : List.of(
                By.xpath(".//span[normalize-space(.)='Hepsini seç']"),
                By.xpath(".//*[contains(normalize-space(.),'Hepsini seç')]"),
                By.xpath(".//span[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='select all']"),
                By.xpath(".//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'select all')]")
        )) {
            List<WebElement> found = popup.findElements(sel);
            if (!found.isEmpty()) return found.get(0);
        }
        return null;
    }

    private WebElement findClearAllElement(WebElement popup) {
        for (By sel : List.of(
                By.xpath(".//*[contains(normalize-space(.),'Tümünün seçimini kaldır')]"),
                By.xpath(".//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'clear all')]")
        )) {
            List<WebElement> found = popup.findElements(sel);
            if (!found.isEmpty()) return found.get(0);
        }
        return null;
    }

    private void clickSelectAll(WebElement popup) {
        WebElement el = findSelectAllElement(popup);
        if (el != null) { safeClick(el); return; }
        for (WebElement li : popup.findElements(By.cssSelector("li"))) {
            String cls = li.getAttribute("class");
            boolean selected = cls != null && cls.contains("e-active");
            if (!selected) safeClick(li);
        }
    }

    private void clearAllIfVisible(WebElement popup) {
        WebElement clear = findClearAllElement(popup);
        if (clear != null) safeClick(clear);
    }

    private void ensureAllSelected(String labelText) {
        WebElement popup = openMultiSelectAndGetPopup(labelText);
        if (!popupShowsClearAll(popup)) clickSelectAll(popup);
        closePopupWithEsc();
    }

    public void selectAllAppointmentTypes()   { ensureAllSelected("Randevu Tipi"); }
    public void ensureAllPlatformsSelected()  { ensureAllSelected("Platform"); }
    public void selectAllDepartments()        { ensureAllSelected("Departman"); }

    public void selectAppointmentTypesByTexts(String csv) { selectFromMultiselectByTexts("Randevu Tipi", csv); }
    public void selectDepartmentsByTexts(String csv)      { selectFromMultiselectByTexts("Departman", csv); }

    /** İstenen seçenek(ler)i seçer. Açıldığında "Tümünün seçimini kaldır" görünüyorsa önce temizler. */
    private void selectFromMultiselectByTexts(String labelText, String csv) {
        if (csv == null || csv.trim().isEmpty()) return;

        WebElement popup = openMultiSelectAndGetPopup(labelText);
        if (popupShowsClearAll(popup)) clearAllIfVisible(popup);

        for (String t : csv.split(",")) {
            String text = t.trim();
            if (text.isEmpty()) continue;

            WebElement li = findListItemByText(popup, text);
            if (li == null) li = scrollUntilItem(popup, text, 24);
            if (li == null) throw new NoSuchElementException(labelText + " içinde bulunamadı: " + text);

            String cls = li.getAttribute("class");
            boolean selected = cls != null && cls.contains("e-active");
            if (!selected) safeClick(li);
        }
        closePopupWithEsc();
    }

    /* ============================================================
                              KAYDET
       ============================================================ */

    /** Sadece “Kaydet”. */
    public void clickSave() {
        ensureVisible();
        retryOnStale(() -> {
            WebElement save = dlgContent().findElement(By.xpath(".//button[normalize-space(.)='Kaydet']"));
            safeClick(save);
            return true;
        });
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.invisibilityOf(freshModalRoot()));
        } catch (Exception ignore) {}
    }

    /** “Kaydet” ve randevular sayfasına dön. */
    public void clickSaveAndReturnToAppointments() {
        clickSave();

        final String targetPath = "/appointment-service/appointments";
        final String targetUrl  = "https://testapp.doctorin.app" + targetPath;

        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(30));
        boolean redirected = false;
        try {
            w.until((ExpectedCondition<Boolean>) d ->
                    d.getCurrentUrl() != null && d.getCurrentUrl().contains(targetPath));
            redirected = true;
        } catch (Exception ignore) { }

        if (!redirected) {
            driver.navigate().to(targetUrl);
            w.until(ExpectedConditions.urlContains(targetPath));
        }
    }
}
