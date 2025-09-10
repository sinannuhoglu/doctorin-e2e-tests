package com.sinannuhoglu.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Appointment Definitions ekranındaki temel gezinme ve grid işlemleri.
 */
public class AppointmentDefinitionsPage {

    private final WebDriver driver;
    private final Duration TIMEOUT = Duration.ofSeconds(20);
    private final Duration SHORT = Duration.ofSeconds(5);

    public AppointmentDefinitionsPage(WebDriver driver) {
        this.driver = driver;
    }

    // ----------------- Yardımcılar -----------------

    private JavascriptExecutor js() { return (JavascriptExecutor) driver; }

    private WebElement waitClickable(By by, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(by));
    }

    private WebElement waitVisible(By by, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private WebElement firstClickable(long seconds, By... locators) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        for (By by : locators) {
            try { return w.until(ExpectedConditions.elementToBeClickable(by)); }
            catch (Exception ignored) {}
        }
        return null;
    }

    private void safeClick(WebElement el) {
        try { el.click(); return; } catch (Exception ignored) {}
        try { new Actions(driver).moveToElement(el).click().perform(); return; } catch (Exception ignored) {}
        js().executeScript("arguments[0].click();", el);
    }

    /** Metni kıyaslanabilir forma indirger (TR karakter normalizasyonu + trim/lowercase). */
    private static String fold(String s) {
        if (s == null) return "";
        s = s.replace('ı','i').replace('İ','I')
                .replace('ş','s').replace('Ş','S')
                .replace('ğ','g').replace('Ğ','G')
                .replace('ç','c').replace('Ç','C')
                .replace('ö','o').replace('Ö','O')
                .replace('ü','u').replace('Ü','U');
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        return n.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+"," ");
    }

    // ----------------- Menü akışı -----------------

    /** Sol menüden “Tanımlar/Definitions” bağlantısını tıklar (bulunamazsa sessiz geçer). */
    public void openDefinitionsFromSidePanel() {
        new WebDriverWait(driver, SHORT).until(d -> !d.findElements(By.cssSelector("aside,nav")).isEmpty());

        By[] defsLocators = new By[] {
                By.xpath("//aside//a[contains(@href,'appointment-definitions')]"),
                By.xpath("//nav//a[contains(@href,'appointment-definitions')]"),
                By.xpath("//*[self::a or self::button or self::div]" +
                        "[contains(normalize-space(.),'Tanımlar') or contains(normalize-space(.),'Definitions')]"),
                By.id("MenuItem_AppointmentService_AppointmentManagement_AppointmentDefinitions"),
                By.xpath("//a[contains(@id,'AppointmentManagement')][.//span[contains(.,'Tanımlar') or contains(.,'Definitions')]]")
        };

        WebElement defs = firstClickable(3, defsLocators);
        if (defs != null) {
            js().executeScript("arguments[0].scrollIntoView({block:'center'});", defs);
            safeClick(defs);
        }
    }

    /** Tanımlar altında “Kaynaklar/Resources” sayfasını açar ve grid’in yüklendiğini doğrular. */
    public void openResourcesUnderDefinitions() {
        By resources = By.cssSelector(
                "a[href*='appointment-service/appointment-resources'], " +
                        "a#MenuItem_AppointmentService_AppointmentManagement_AppointmentResources"
        );

        WebElement link = firstClickable(5, resources);
        if (link != null) {
            js().executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            safeClick(link);
        } else {
            js().executeScript("window.location.href='/appointment-service/appointment-resources';");
        }

        new WebDriverWait(driver, TIMEOUT).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.e-grid[id='Grid']"))
        );
        new WebDriverWait(driver, TIMEOUT).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.e-gridcontent div.e-content"))
        );
    }

    // ----------------- Grid / Pager locator'ları -----------------

    private final By gridRoot = By.cssSelector("div.e-grid[id='Grid'], div[role='grid'].e-grid");
    private final By gridContent = By.cssSelector("div.e-gridcontent div.e-content");
    private final By spinnerVisible = By.cssSelector(".e-spinner-pane:not(.e-spin-hide)");

    // Pager / page-size dropdown
    private final By pager = By.cssSelector("div.e-pager");
    private final By pageSizeInput = By.cssSelector(".e-pager .e-pagerdropdown input[id^='dropdownlist'][type='text']");
    private final By pageSizeIcon  = By.cssSelector(".e-pager .e-pagerdropdown .e-input-group-icon.e-ddl-icon.e-icons");

    private WebElement gridContent() { return driver.findElement(gridContent); }

    private List<WebElement> visibleRows() {
        return driver.findElements(By.cssSelector("table#Grid_content_table tbody[role='rowgroup'] tr.e-row"));
    }

    // ----------------- Pager: Sayfa başına = 100 -----------------

    public void ensurePagerPageSize(int targetSize) {
        List<WebElement> p = driver.findElements(pager);
        if (p.isEmpty()) return;
        waitVisible(pager, SHORT);

        try {
            WebElement inp = driver.findElement(pageSizeInput);
            String cur = inp.getAttribute("value");
            if (String.valueOf(targetSize).equals(cur != null ? cur.trim() : "")) return;
        } catch (NoSuchElementException ignored) {}

        WebElement icon = waitClickable(pageSizeIcon, TIMEOUT);
        safeClick(icon);

        WebElement popup = waitForOpenDdlPopup();

        WebElement option = findPopupItemByExactText(popup, String.valueOf(targetSize));
        if (option == null) throw new NoSuchElementException("Page size option not found: " + targetSize);
        safeClick(option);

        new WebDriverWait(driver, TIMEOUT).until(ExpectedConditions.invisibilityOf(popup));
        waitUntilGridIdle();
    }

    private WebElement waitForOpenDdlPopup() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    List<WebElement> pops = d.findElements(By.cssSelector(
                            "div.e-ddl.e-control.e-lib.e-popup.e-popup-open[role='dialog'], " +
                                    "div.e-dropdownbase.e-popup-open[role='dialog']"
                    ));
                    if (pops.isEmpty()) return null;
                    WebElement last = null;
                    for (WebElement pop : pops) {
                        try { if (pop.isDisplayed()) last = pop; } catch (Exception ignored) {}
                    }
                    return last != null ? last : pops.get(pops.size() - 1);
                });
    }

    private WebElement findPopupItemByExactText(WebElement popup, String wanted) {
        for (WebElement li : popup.findElements(By.cssSelector("li, div.e-list-item, span, label"))) {
            String t = li.getText() != null ? li.getText().trim() : "";
            if (t.equals(wanted)) return li;
        }
        for (WebElement li : popup.findElements(By.cssSelector("li"))) {
            String t = li.getText() != null ? li.getText().trim() : "";
            if (t.equals(wanted)) return li;
        }
        return null;
    }

    private void waitUntilGridIdle() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> d.findElements(spinnerVisible).isEmpty());
        waitVisible(gridRoot, SHORT);
        try { Thread.sleep(120); } catch (InterruptedException ignored) {}
    }

    // ----------------- Switch kontrolü (aria-colindex=4) -----------------

    /**
     * Verilen satırdaki 4. sütunda yer alan switch'i (div.e-switch-wrapper) aktif değilse aktif eder.
     * Aktifse/dosyada yoksa dokunmaz. Disabled ise herhangi bir işlem yapmaz.
     * @return switch aktif veya yoksa true, aksi halde false (ör. disabled kaldı)
     */
    private boolean ensureRowSwitchActive(WebElement row) {
        try {
            WebElement switchCell = row.findElement(By.cssSelector("td.e-rowcell[aria-colindex='4']"));
            List<WebElement> wrappers = switchCell.findElements(By.cssSelector("div.e-switch-wrapper"));
            if (wrappers.isEmpty()) return true; // bu satırda switch yok → sorun değil

            WebElement wrapper = wrappers.get(0);
            js().executeScript("arguments[0].scrollIntoView({block:'center'});", wrapper);

            boolean disabled = "true".equalsIgnoreCase(wrapper.getAttribute("aria-disabled"))
                    || wrapper.getAttribute("class").contains("e-switch-disabled");
            if (disabled) return false;

            WebElement input = switchCell.findElement(By.cssSelector("input[type='checkbox']"));
            boolean active = wrapper.getAttribute("class").contains("e-switch-active") || input.isSelected();
            if (active) return true;

            List<WebElement> handles = wrapper.findElements(By.cssSelector(".e-switch-handle, .e-switch-inner"));
            WebElement clickTarget = handles.isEmpty() ? wrapper : handles.get(0);
            safeClick(clickTarget);

            new WebDriverWait(driver, SHORT).until(d ->
                    wrapper.getAttribute("class").contains("e-switch-active") ||
                            switchCell.findElement(By.cssSelector("input[type='checkbox']")).isSelected()
            );
            return true;
        } catch (NoSuchElementException e) {
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ----------------- Grid işlemleri -----------------

    /**
     * Grid’de ilk sütunda adı verilen kaynağı bularak aynı satırdaki switch'i (4. sütun) gerekirse aktif eder
     * ve ardından “Düzenle” butonuna tıklar.
     * Sanallaştırılmış gridlerde görünür alanı tarar. Aramadan önce pager'da “sayfa başına” değeri 100’e çekilir.
     */
    public void clickEditForResourceByName(String displayName) {
        ensurePagerPageSize(100);

        String target = fold(Objects.requireNonNull(displayName, "displayName"));

        WebElement content = gridContent();
        long lastScrollTop = -1;
        int guard = 0;

        while (guard++ < 200) {
            for (WebElement row : visibleRows()) {
                List<WebElement> cells = row.findElements(By.cssSelector("td.e-rowcell"));
                if (cells.isEmpty()) continue;

                String firstCellText = fold(cells.get(0).getText());
                if (firstCellText.contains(target)) {

                    ensureRowSwitchActive(row);

                    WebElement actionsCell;
                    try {
                        WebElement btn = row.findElement(By.xpath(".//button[@title='Düzenle' or contains(@class,'e-editbutton')]"));
                        js().executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                        safeClick(btn);
                    } catch (NoSuchElementException e) {
                        actionsCell = cells.size() >= 5
                                ? cells.get(4)
                                : row.findElement(By.cssSelector("td.e-rowcell.e-unboundcell.e-rightalign"));
                        By editBtn = By.xpath(".//button[@title='Düzenle' or contains(@class,'e-editbutton') or .//span[contains(@class,'e-edit')]]");
                        WebElement btn = new WebDriverWait(driver, SHORT)
                                .until(ExpectedConditions.elementToBeClickable(actionsCell.findElement(editBtn)));
                        js().executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                        safeClick(btn);
                    }

                    new WebDriverWait(driver, TIMEOUT).until(d ->
                            !d.findElements(By.cssSelector("div.e-dlg-container.appointment-resources__dialog, div.e-dialog.e-dlg-modal")).isEmpty()
                    );
                    return;
                }
            }

            Long st = (Long) js().executeScript(
                    "var el=arguments[0];var max=el.scrollHeight-el.clientHeight;" +
                            "if(el.scrollTop>=max){return -1;} " +
                            "el.scrollTop=Math.min(el.scrollTop+Math.max(100, el.clientHeight*0.8), max); " +
                            "return el.scrollTop;",
                    content
            );
            if (st == null || st == -1 || st == lastScrollTop) break;
            lastScrollTop = st;
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        throw new NoSuchElementException("Kaynak bulunamadı: " + displayName);
    }

    // ----------------- Dialog / Sekme -----------------

    /** Açık diyalogta verilen sekme başlığına tıklar. */
    public void clickTabInDialog(String tabText) {
        String wanted = fold(tabText);

        By tabBar = By.cssSelector("div.e-control.e-toolbar.e-lib.e-tab-header");
        new WebDriverWait(driver, TIMEOUT).until(ExpectedConditions.presenceOfElementLocated(tabBar));

        List<WebElement> tabs = driver.findElements(By.cssSelector("div.e-hscroll-content .e-tab-text"));
        if (tabs.isEmpty()) tabs = driver.findElements(By.cssSelector("div.e-tab-header *"));

        for (WebElement t : tabs) {
            String txt = fold(t.getText());
            if (txt.contains(wanted)) {
                js().executeScript("arguments[0].scrollIntoView({inline:'center',block:'center'});", t);
                safeClick(t);
                return;
            }
        }
        throw new NoSuchElementException("Sekme bulunamadı: " + tabText);
    }

    /** “Çalışma Takvimi” sekmesine kısayol. */
    public void openWorkplanTab() {
        clickTabInDialog("Çalışma Takvimi");
    }
}
