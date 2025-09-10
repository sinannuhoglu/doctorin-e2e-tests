package com.sinannuhoglu.pages;

import com.sinannuhoglu.core.BasePage;
import com.sinannuhoglu.core.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AppointmentsPage extends BasePage {

    public AppointmentsPage(WebDriver driver, ConfigReader cfg) { super(driver, cfg); }
    public AppointmentsPage(WebDriver driver) { super(driver); }

    // Filters
    private final String[] BRANCH_TESTIDS = {"location-filter", "branch-filter", "sube-filter"};
    private final String[] DEPT_TESTIDS   = {"department-filter", "departman-filter"};
    private final String[] DOCTOR_TESTIDS = {"doctor-filter", "kaynaklar-filter", "resource-filter"};

    private final By PANEL_SENTINEL = By.cssSelector("[data-testid='filter-buttons'],[data-testid='department-filter'],[data-testid='doctor-filter']");
    private final By FILTER_BUTTONS = By.cssSelector("[data-testid='filter-buttons']");
    private final By ACCEPT_BUTTON  = By.cssSelector("[data-testid='accept-button'], button.e-primary");
    private final By APPLY_BTN_FALLBACK = By.xpath("//button[normalize-space()='Kabul et' or normalize-space()='Uygula' or normalize-space()='Kaydet']");
    private final By PAGE_ANCHOR    = By.xpath("//main|//div[@id='appointments']|//h2[contains(.,'Randevu')]");

    // Schedule (Syncfusion)
    private final By CONTENT_WRAP        = By.cssSelector("div[id^='Schedule-'] .e-table-container .e-content-wrap");
    private final By DAY_VIEW_ROWS       = By.cssSelector("div[id^='Schedule-'] .e-table-container .e-content-wrap table.e-content-table tbody[role='rowgroup'] tr");
    private final By WORK_CELL_GROUP0    = By.cssSelector("td.e-work-cells[data-group-index='0']");
    private final By WORK_CELL_DEFAULT   = By.cssSelector("td.e-work-cells");

    // Sidebar / Patient search
    private final By SIDEBAR_TITLE   = By.cssSelector("[data-testid='sidebar-title']");
    private final By SIDEBAR_CONTENT = By.cssSelector("[data-testid='sidebar-content'], .sidebar-main-section");
    private final By PATIENT_SEARCH_INPUT = By.cssSelector("input[data-testid='appointment-patient-search'], #appointment-patient-search");
    private final By SEARCH_BUTTON        = By.xpath("//button[normalize-space()='Ara' or @type='submit']");

    // Popover & form/save
    private final By POPOVER_ROOT = By.cssSelector("[data-testid='popover-template']");
    private final By PATIENT_RESULT_ITEMS_ANYWHERE = By.cssSelector(
            "[data-testid^='patient-item-'], " +
                    "[data-testid='popover-template'] [data-testid^='patient-item-'], " +
                    ".e-list-item, .cursor-pointer, .flex.flex-col.divide-y > *"
    );
    private final By PATIENT_RESULT_NAME = By.cssSelector("[data-testid^='patient-name-'], p[title]");
    private final By FORM_CONTAINER = By.cssSelector("form.e-control.e-control-container, [id^='dataform-'], [data-testid='appointment-form-section']");
    private final By FORM_ACTIONS   = By.cssSelector("[data-testid='form-actions']");
    private final By SAVE_BUTTON    = By.cssSelector("button[type='submit'], [data-testid='save_button'], [data-testid='save-button']");
    private final By SAVE_FALLBACK  = By.xpath("//button[normalize-space()='Kaydet' or normalize-space()='KAYDET']");

    // Appointment tiles / quick popup
    private final By APPOINTMENT_TILE_ANY = By.cssSelector("div.e-appointment.e-lib.e-draggable[role='button'][data-group-index='0']");
    private final By APPOINTMENT_TILE_BORDERED = By.cssSelector("div.e-appointment.e-lib.e-draggable.e-appointment-border[role='button'][data-group-index='0']");
    private final By APPOINTMENT_DETAILS = By.cssSelector(".e-appointment-details");
    private final By APPOINTMENT_EVENT_ICON = By.cssSelector("[data-testid='event-icon']");
    private final By QUICK_POPUP = By.cssSelector(".e-quick-popup-wrapper.e-lib.e-popup[role='dialog']");
    private final By QUICK_POPUP_HEADER = By.cssSelector(".e-popup-header-title-text, [data-testid='quick-info-header'] .font-semibold");
    private final By QUICK_POPUP_CLOSE = By.cssSelector("button[data-testid='quick-info-close-button'], .e-quick-popup-wrapper .e-icon-btn");
    private final By APPOINTMENT_FOOTER = By.cssSelector("[data-testid='appointment-footer']");
    private final By CHECKIN_BUTTON = By.cssSelector(
            "[data-testid='appointment-footer'] button[data-testid='status-button'], " +
                    ".e-quick-popup-wrapper [data-testid='status-button']"
    );
    private final By QUICK_POPUP_STATUS = By.xpath(
            "//div[contains(@class,'e-quick-popup-wrapper')]//p[contains(@class,'text-surface-500') and contains(@class,'text-xs') and contains(@class,'truncate')]"
    );

    // Delete modals
    private final By QUICK_DELETE_BUTTON = By.cssSelector(".e-quick-popup-wrapper [data-testid='appointment-delete-button']");
    private final By MODAL_CONTAINER = By.cssSelector("div[id^='modal-dialog-'], .e-dlg-container .e-dialog");
    private final By MODAL_YES_TEXT = By.xpath(".//div[contains(@class,'e-footer-content')]//button[normalize-space()='Evet']");
    private final By MODAL_PRIMARY_IN_FOOTER = By.cssSelector(".e-footer-content button.e-primary");
    private final By MODAL_OKAY_BUTTON = By.cssSelector(".e-footer-content #okay-button");
    private final By MODAL_OKAY_TEXT = By.xpath(".//div[contains(@class,'e-footer-content')]//button[normalize-space()='Tamam']");

    // Last clicked slot
    private Integer lastSlotHour = null;
    private Boolean lastSlotFirstHalf = null;

    private String startTextHHmm() {
        if (lastSlotHour == null || lastSlotFirstHalf == null) return null;
        int h = lastSlotHour, m = lastSlotFirstHalf ? 0 : 30;
        return String.format("%02d:%02d", h, m);
    }
    private long startDurationMillis() {
        if (lastSlotHour == null || lastSlotFirstHalf == null)
            throw new IllegalStateException("Slot bilgisi yok. Önce bir slot'a tıklanmalı.");
        int minutes = (lastSlotHour * 60) + (lastSlotFirstHalf ? 0 : 30);
        return minutes * 60_000L;
    }

    /** Sayfanın yüklendiğini doğrular. */
    public AppointmentsPage assertLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_ANCHOR));
        return this;
    }

    private boolean isFilterPanelOpen() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(2)).until(d -> {
                List<WebElement> els = d.findElements(PANEL_SENTINEL);
                return !els.isEmpty() && els.get(0).isDisplayed();
            });
        } catch (TimeoutException e) { return false; }
    }
    private void ensureFilterPanelOpen() { if (!isFilterPanelOpen()) openFilterPanel(); }

    private WebElement findFilterToggleButton() {
        List<By> tries = List.of(
                By.xpath("(//*[not(ancestor::*[@role='dialog' or contains(@class,'drawer') or contains(@class,'modal')])]" +
                        "[(self::button or self::a) and (normalize-space()='Filtre' or normalize-space()='Filtrele' or .//span[normalize-space()='Filtre' or normalize-space()='Filtrele'])])[1]"),
                By.xpath("(//button[contains(.,'Filtre') or contains(.,'Filtrele')])[1]")
        );
        for (By by : tries) {
            List<WebElement> found = driver.findElements(by);
            if (!found.isEmpty()) return found.get(0);
        }
        throw new NoSuchElementException("'Filtre/Filtrele' butonu bulunamadı.");
    }

    /** Filtre panelini açar. */
    public void openFilterPanel() {
        if (isFilterPanelOpen()) return;
        WebElement toggle = findFilterToggleButton();
        wait.until(ExpectedConditions.elementToBeClickable(toggle));
        try { toggle.click(); } catch (ElementClickInterceptedException e) { jsClick(toggle); }
        if (!isFilterPanelOpen()) throw new TimeoutException("Filtre paneli açılamadı.");
    }

    private WebElement getBlockByTestIds(String[] ids) {
        for (String id : ids) {
            List<WebElement> found = driver.findElements(By.cssSelector("[data-testid='" + id + "']"));
            if (!found.isEmpty() && found.get(0).isDisplayed()) return found.get(0);
        }
        return null;
    }
    private boolean isDisabled(WebElement block) {
        try {
            String cls = block.getAttribute("class");
            if (cls != null && cls.contains("e-disabled")) return true;
            if (!block.findElements(By.cssSelector(".e-disabled,[aria-disabled='true']")).isEmpty()) return true;
            WebElement input = block.findElement(By.cssSelector("input"));
            return "true".equalsIgnoreCase(input.getAttribute("aria-disabled"));
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    private WebDriverWait waitTiny()   { return new WebDriverWait(driver, Duration.ofSeconds(2)); }
    private WebDriverWait waitShort()  { return new WebDriverWait(driver, Duration.ofSeconds(5)); }
    private WebDriverWait waitMedium() { return new WebDriverWait(driver, Duration.ofSeconds(20)); }
    private WebDriverWait waitLong()   { return new WebDriverWait(driver, Duration.ofSeconds(60)); }

    public void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }
    @Override
    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('mouseover',{bubbles:true}));" +
                        "arguments[0].click();", el
        );
    }

    private static String normalizeLike(String s) {
        if (s == null) return "";
        String t = s.trim().replace('\u00A0', ' ').replaceAll("\\s+", " ");
        t = t.replace('İ','I').replace('ı','i');
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        return t.toLowerCase();
    }
    private static boolean textLike(String a, String b) {
        String na = normalizeLike(a), nb = normalizeLike(b);
        return na.equals(nb) || na.contains(nb) || nb.contains(na);
    }

    // ---------- Dropdown helpers ----------
    private WebElement detectOpenDropdownPopup() {
        return waitShort().until(d -> {
            List<WebElement> candidates = new ArrayList<>();
            candidates.addAll(d.findElements(By.cssSelector("div[id$='_popup'].e-popup")));
            candidates.addAll(d.findElements(By.cssSelector(".e-popup-open")));
            candidates.addAll(d.findElements(By.cssSelector(".e-dropdownbase .e-content")));
            candidates.addAll(d.findElements(By.cssSelector("ul[role='listbox']")));
            candidates.addAll(d.findElements(By.cssSelector(".e-list-parent")));
            for (WebElement c : candidates) {
                try {
                    if (!c.isDisplayed()) continue;
                    for (WebElement li : c.findElements(By.cssSelector("li[role='option'], .e-list-item"))) {
                        if (li.isDisplayed() && li.getRect().getHeight() > 0) return c;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
            return null;
        });
    }

    private WebElement openDropDownIn(String[] possibleTestIds) {
        ensureFilterPanelOpen();
        wait.until(d -> {
            WebElement b = getBlockByTestIds(possibleTestIds);
            return b != null && !isDisabled(b);
        });

        WebElement block = Objects.requireNonNull(getBlockByTestIds(possibleTestIds), "Dropdown bloğu bulunamadı");
        scrollIntoView(block);

        WebElement icon = null, input = null;
        List<WebElement> icons = block.findElements(By.cssSelector(".e-input-group-icon.e-ddl-icon, .e-ddl-icon"));
        if (!icons.isEmpty()) icon = icons.get(0);
        List<WebElement> inputs = block.findElements(By.cssSelector("input[id]"));
        if (!inputs.isEmpty()) input = inputs.get(0);

        for (int attempt = 0; attempt < 3; attempt++) {
            WebElement clickTarget = (attempt == 0 && icon != null) ? icon
                    : (attempt == 1 && input != null) ? input
                    : block;

            try { wait.until(ExpectedConditions.elementToBeClickable(clickTarget)).click(); }
            catch (Exception e) { jsClick(clickTarget); }

            WebElement popup = null;
            try {
                if (input != null) {
                    String id = input.getAttribute("id");
                    if (id != null) {
                        for (WebElement p : driver.findElements(By.id(id + "_popup")))
                            if (p.isDisplayed()) popup = p;
                    }
                    if (popup == null) {
                        String owns = input.getAttribute("aria-owns");
                        if (owns != null && !owns.isBlank()) {
                            for (WebElement p : driver.findElements(By.id(owns + "_popup")))
                                if (p.isDisplayed()) popup = p;
                        }
                    }
                }
            } catch (StaleElementReferenceException ignored) {}

            if (popup == null) {
                try { popup = detectOpenDropdownPopup(); } catch (TimeoutException ignored) {}
            }

            if (popup != null) {
                try {
                    WebElement finalPopup = popup;
                    WebElement ready = waitShort().until(d -> {
                        for (WebElement li : finalPopup.findElements(By.cssSelector("li[role='option'], .e-list-item")))
                            if (li.isDisplayed() && li.getRect().getHeight() > 0) return li;
                        return null;
                    });
                    if (ready != null) return popup;
                } catch (TimeoutException ignored) {}
            }
            try { Thread.sleep(250); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        throw new TimeoutException("Dropdown popup açılmadı.");
    }

    private void chooseFromPopupAndConfirm(WebElement block, WebElement popup, String optionText) {
        By liBy = By.xpath(".//li[@role='option' or contains(@class,'e-list-item')][contains(normalize-space(),'"
                + optionText + "')]");
        WebElement li = wait.until(d -> {
            try {
                WebElement el = popup.findElement(liBy);
                return el.isDisplayed() ? el : null;
            } catch (NoSuchElementException e) { return null; }
        });
        try { li.click(); } catch (Exception e) { jsClick(li); }

        WebElement input = block.findElement(By.cssSelector("input"));
        wait.until(d -> {
            String val = input.getAttribute("value");
            String expanded = block.getAttribute("aria-expanded");
            boolean okVal = val != null && val.trim().equalsIgnoreCase(optionText.trim());
            boolean closed = !"true".equalsIgnoreCase(expanded);
            return okVal && closed;
        });
    }

    private void assertInputValueIn(String[] possibleTestIds, String expected) {
        WebElement block = Objects.requireNonNull(getBlockByTestIds(possibleTestIds), "Beklenen alan bulunamadı.");
        WebElement input = Objects.requireNonNull(block.findElement(By.cssSelector("input")), "Input yok");
        wait.until(d -> expected.equalsIgnoreCase(input.getAttribute("value").trim()));
    }

    /** Şube seçer ve bağımlı alanların aktif olmasını bekler. */
    public void selectBranch(String branchName) {
        WebElement block = Objects.requireNonNull(getBlockByTestIds(BRANCH_TESTIDS), "Şube alanı bulunamadı");
        WebElement popup = openDropDownIn(BRANCH_TESTIDS);
        chooseFromPopupAndConfirm(block, popup, branchName);
        assertInputValueIn(BRANCH_TESTIDS, branchName);
        wait.until(d -> {
            WebElement b = getBlockByTestIds(DEPT_TESTIDS);
            return b != null && !isDisabled(b);
        });
        wait.until(d -> {
            WebElement b = getBlockByTestIds(DOCTOR_TESTIDS);
            return b != null && !isDisabled(b);
        });
    }

    /** Şube alanının değerini doğrular. */
    public void assertBranchValue(String expected) {
        ensureFilterPanelOpen();
        assertInputValueIn(BRANCH_TESTIDS, expected);
    }

    /** Departman seçer ve doktor alanının aktif olmasını bekler. */
    public void selectDepartment(String deptName) {
        WebElement block = Objects.requireNonNull(getBlockByTestIds(DEPT_TESTIDS), "Departman alanı bulunamadı");
        WebElement popup = openDropDownIn(DEPT_TESTIDS);
        chooseFromPopupAndConfirm(block, popup, deptName);
        assertInputValueIn(DEPT_TESTIDS, deptName);
        wait.until(d -> {
            WebElement b = getBlockByTestIds(DOCTOR_TESTIDS);
            return b != null && !isDisabled(b);
        });
    }

    // ---------- Chips & Apply ----------
    private List<WebElement> getDoctorChips(WebElement doctorBlock) {
        return new ArrayList<>(doctorBlock.findElements(By.cssSelector(".e-chips-collection .e-chips")));
    }
    private String chipLabel(WebElement chip) {
        try {
            String title = chip.getAttribute("title");
            if (title != null && !title.isBlank()) return title.trim();
        } catch (Exception ignored) {}
        try { return chip.findElement(By.cssSelector(".e-chipcontent")).getText().trim(); }
        catch (Exception ignored) {}
        return chip.getText().trim();
    }
    private void closeChip(WebElement chip, WebElement doctorBlock) {
        int before = getDoctorChips(doctorBlock).size();
        WebElement close = chip.findElement(By.cssSelector(".e-chips-close"));
        try { wait.until(ExpectedConditions.elementToBeClickable(close)).click(); }
        catch (Exception e) { jsClick(close); }
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
            try { return getDoctorChips(doctorBlock).size() < before || !chip.isDisplayed(); }
            catch (StaleElementReferenceException ex) { return true; }
        });
    }
    private void clickAcceptAndWaitClose() {
        if (!isFilterPanelOpen()) return;
        WebElement btn = null;
        List<WebElement> first = driver.findElements(ACCEPT_BUTTON);
        if (!first.isEmpty()) btn = first.get(0);
        if (btn == null) {
            List<WebElement> fb = driver.findElements(APPLY_BTN_FALLBACK);
            if (!fb.isEmpty()) btn = fb.get(0);
        }
        if (btn == null) throw new NoSuchElementException("'Kabul et/Uygula/Kaydet' butonu bulunamadı.");
        try { wait.until(ExpectedConditions.elementToBeClickable(btn)).click(); }
        catch (Exception e) { jsClick(btn); }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(FILTER_BUTTONS));
    }

    /** Yalnızca verilen doktor chip’i kalacak şekilde diğerlerini kapatır ve uygular. */
    public void keepOnlyDoctor(String doctorFullName) {
        ensureFilterPanelOpen();
        wait.until(d -> {
            WebElement b = getBlockByTestIds(DOCTOR_TESTIDS);
            return b != null && !isDisabled(b);
        });
        WebElement block = Objects.requireNonNull(getBlockByTestIds(DOCTOR_TESTIDS), "Kaynaklar (doktor) alanı bulunamadı.");
        scrollIntoView(block);

        for (int guard = 0; guard < 20; guard++) {
            List<WebElement> chips = getDoctorChips(block);
            if (chips.isEmpty()) break;

            WebElement toClose = null;
            int targetCount = 0;
            for (WebElement chip : chips) {
                String label = chipLabel(chip);
                if (label.equalsIgnoreCase(doctorFullName.trim())) targetCount++;
                else { toClose = chip; break; }
            }

            if (toClose != null) { closeChip(toClose, block); continue; }
            if (targetCount > 1) { closeChip(chips.get(0), block); continue; }

            if (chips.size() == 1 && targetCount == 1) {
                clickAcceptAndWaitClose();
                return;
            }
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        throw new AssertionError("Beklenen tek chip bulunamadı: " + doctorFullName);
    }

    /** Filtre değişikliklerini uygular. */
    public void applyFilters() {
        if (!isFilterPanelOpen()) return;
        clickAcceptAndWaitClose();
    }

    // ---------- Slot click & drawer ----------

    /** Grid satırları `:30` ile mi başlıyor, `:00` ile mi? (dinamik tespit) */
    private boolean isHalfHourRowFirst() {
        try {
            WebElement wrap = waitVisible(CONTENT_WRAP);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop=0;", wrap);
        } catch (Exception ignored) {}

        List<WebElement> rows = dayRows();
        if (rows == null || rows.isEmpty()) return false;

        WebElement tr = rows.get(0);
        WebElement td;
        List<WebElement> group0 = tr.findElements(WORK_CELL_GROUP0);
        if (!group0.isEmpty()) td = group0.get(0);
        else {
            List<WebElement> any = tr.findElements(WORK_CELL_DEFAULT);
            td = !any.isEmpty() ? any.get(0) : tr.findElement(By.cssSelector("td"));
        }

        String al = null;
        try { al = td.getAttribute("aria-label"); } catch (Exception ignored) {}
        if (al == null || al.isBlank()) {
            try { al = td.getText(); } catch (Exception ignored) {}
        }
        if (al == null) return false;

        return al.contains(":30") || al.contains(".30");
    }

    /** 0..23 saat, firstHalf=true => HH:00; false => HH:30. Dinamik sıra ile 1-bazlı satır index’i. */
    private int trIndexForHourDynamic(int hour, boolean firstHalf) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("hour 0..23 olmalı");
        boolean halfFirst = isHalfHourRowFirst();
        int base = 2 * hour;
        if (halfFirst) {
            return base + (firstHalf ? 2 : 1);
        } else {
            return base + (firstHalf ? 1 : 2);
        }
    }

    private List<WebElement> dayRows() {
        return wait.until(d -> {
            List<WebElement> rows = d.findElements(DAY_VIEW_ROWS);
            return (rows != null && rows.size() >= 48) ? rows : null;
        });
    }
    private WebElement rowTdAt(int oneBasedIndex) {
        List<WebElement> rows = dayRows();
        if (rows == null || rows.size() < oneBasedIndex)
            throw new NoSuchElementException("Satır index bulunamadı: " + oneBasedIndex);
        WebElement tr = rows.get(oneBasedIndex - 1);
        try {
            WebElement wrap = waitVisible(CONTENT_WRAP);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = Math.max(arguments[1]-100,0);",
                    wrap, tr.getLocation().getY());
        } catch (Exception ignore) { scrollIntoView(tr); }
        List<WebElement> group0 = tr.findElements(WORK_CELL_GROUP0);
        if (!group0.isEmpty()) return group0.get(0);
        List<WebElement> any = tr.findElements(WORK_CELL_DEFAULT);
        if (!any.isEmpty()) return any.get(0);
        return tr.findElement(By.cssSelector("td"));
    }

    private void clickCellAndWaitSidebar(WebElement td) {
        try { wait.until(ExpectedConditions.elementToBeClickable(td)).click(); }
        catch (Exception e) { jsClick(td); }

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(SIDEBAR_TITLE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIDEBAR_CONTENT));
        wait.until(d -> {
            try {
                if (!title.isDisplayed()) return false;
                WebElement container = d.findElement(SIDEBAR_CONTENT);
                String style = container.getAttribute("style");
                boolean opaque = style == null || !style.contains("opacity: 0");
                boolean notHidden = style == null || !style.contains("display: none");
                return container.isDisplayed() && opaque && notHidden;
            } catch (Exception ex) { return false; }
        });
    }

    /** Görünen alanda tüm TD’leri gezerek hedef zamanı bulan (XPath’siz) tarayıcı. Bulamazsa kaydırarak devam eder. */
    private WebElement findCellByTimeScrolling(int hour24, int minute) {
        String hmColon = String.format("%02d:%02d", hour24, minute);
        String hmDot   = String.format("%02d.%02d", hour24, minute);

        WebElement wrap = waitVisible(CONTENT_WRAP);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop=0;", wrap);

        long last = -1;
        for (int guard = 0; guard < 220; guard++) {
            List<WebElement> visibleCells = driver.findElements(By.cssSelector("td.e-work-cells[data-group-index='0']"));
            for (WebElement el : visibleCells) {
                try {
                    if (!el.isDisplayed() || el.getRect().getHeight() <= 0) continue;
                    String al = el.getAttribute("aria-label");
                    String txt = el.getText();
                    if ((al != null && (al.contains(hmColon) || al.contains(hmDot))) ||
                            (txt != null && (txt.contains(hmColon) || txt.contains(hmDot)))) {
                        return el;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }

            Long st = (Long) ((JavascriptExecutor) driver).executeScript(
                    "var el=arguments[0]; var max=el.scrollHeight-el.clientHeight;" +
                            "if(el.scrollTop>=max){return -1;} el.scrollTop=Math.min(el.scrollTop+Math.max(100, el.clientHeight*0.8), max); return el.scrollTop;",
                    wrap
            );
            if (st == null || st == -1 || st == last) break;
            last = st;
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    /** Grid’de slot’a tıklar ve drawer’ın görünmesini bekler. */
    public void clickSlot(int hour, boolean firstHalf) {
        this.lastSlotHour = hour;
        this.lastSlotFirstHalf = firstHalf;

        int idx = trIndexForHourDynamic(hour, firstHalf);
        WebElement td = rowTdAt(idx);
        clickCellAndWaitSidebar(td);
    }

    /** 09:00-09:30. */
    public void clickNineAmFirstSlot() { clickSlot(9, true); }

    // ---------- Patient search & quick popup ----------
    private void clickSearchButton() {
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(SIDEBAR_CONTENT));
        List<WebElement> inScope = sidebar.findElements(SEARCH_BUTTON);
        WebElement btn = !inScope.isEmpty() ? inScope.get(0) : null;
        if (btn == null) {
            List<WebElement> global = driver.findElements(SEARCH_BUTTON);
            if (!global.isEmpty()) btn = global.get(0);
        }
        if (btn == null) throw new NoSuchElementException("'Ara' butonu bulunamadı.");
        try { waitShort().until(ExpectedConditions.elementToBeClickable(btn)).click(); }
        catch (Exception e) { jsClick(btn); }
    }

    private void waitResultsAppear() {
        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
        waitMedium().until(d -> {
            for (WebElement el : d.findElements(PATIENT_RESULT_NAME)) {
                try { if (el.isDisplayed() && el.getRect().getHeight() > 0) return true; } catch (Exception ignored) {}
            }
            for (WebElement el : d.findElements(PATIENT_RESULT_ITEMS_ANYWHERE)) {
                try { if (el.isDisplayed() && el.getRect().getHeight() > 0) return true; } catch (Exception ignored) {}
            }
            return false;
        });
    }

    private void clickPatientResultItem(WebElement sidebar, String fullName) {
        WebElement root = null;
        List<WebElement> pop = driver.findElements(POPOVER_ROOT);
        if (!pop.isEmpty() && pop.get(0).isDisplayed()) root = pop.get(0);
        if (root == null) root = sidebar;

        WebElement finalRoot = root;
        List<WebElement> nameEls = waitMedium().until(d -> {
            List<WebElement> els = new ArrayList<>();
            els.addAll(finalRoot.findElements(PATIENT_RESULT_NAME));
            if (els.isEmpty()) els.addAll(d.findElements(PATIENT_RESULT_NAME));
            els.removeIf(e -> {
                try { return !e.isDisplayed() || e.getRect().getHeight() <= 0; }
                catch (Exception ex) { return true; }
            });
            return els.isEmpty() ? null : els;
        });

        WebElement nameTarget = null;
        for (WebElement e : nameEls) {
            String t = e.getAttribute("title");
            if (t == null || t.isBlank()) t = e.getText();
            if (t != null && textLike(t, fullName)) { nameTarget = e; break; }
        }
        if (nameTarget == null) nameTarget = nameEls.get(0);

        WebElement clickTarget = nameTarget;
        for (int i = 0; i < 6; i++) {
            if (clickTarget == null) break;
            String dt = "";
            try { dt = clickTarget.getAttribute("data-testid"); } catch (Exception ignored) {}
            String cls = "";
            try { cls = clickTarget.getAttribute("class"); } catch (Exception ignored) {}
            if ((dt != null && dt.startsWith("patient-item-")) || (cls != null && cls.contains("cursor-pointer"))) break;
            try { clickTarget = clickTarget.findElement(By.xpath("..")); }
            catch (Exception ex) { break; }
        }
        if (clickTarget == null) clickTarget = nameTarget;

        try { scrollIntoView(clickTarget); } catch (Exception ignored) {}
        boolean clicked = false;
        try {
            new Actions(driver).moveToElement(clickTarget).pause(Duration.ofMillis(80)).click().perform();
            clicked = true;
        } catch (Exception ignored) {}
        if (!clicked) {
            try { waitShort().until(ExpectedConditions.elementToBeClickable(clickTarget)).click(); }
            catch (Exception e) { jsClick(clickTarget); }
        }

        try {
            waitMedium().until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOfElementLocated(POPOVER_ROOT),
                    ExpectedConditions.visibilityOfElementLocated(FORM_ACTIONS),
                    ExpectedConditions.visibilityOfElementLocated(FORM_CONTAINER)
            ));
        } catch (TimeoutException te) {
            try { driver.findElement(PATIENT_SEARCH_INPUT).sendKeys(Keys.ENTER); } catch (Exception ignored) {}
            waitMedium().until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOfElementLocated(POPOVER_ROOT),
                    ExpectedConditions.visibilityOfElementLocated(FORM_ACTIONS),
                    ExpectedConditions.visibilityOfElementLocated(FORM_CONTAINER)
            ));
        }
    }

    private void clickSaveOnForm() {
        WebElement actionsArea;
        try {
            actionsArea = waitShort().until(ExpectedConditions.visibilityOfElementLocated(FORM_ACTIONS));
        } catch (TimeoutException e) {
            actionsArea = waitMedium().until(ExpectedConditions.visibilityOfElementLocated(FORM_CONTAINER));
        }
        scrollIntoView(actionsArea);

        WebElement save = null;
        List<WebElement> primary = driver.findElements(SAVE_BUTTON);
        if (!primary.isEmpty()) save = primary.get(0);
        if (save == null) {
            List<WebElement> fb = driver.findElements(SAVE_FALLBACK);
            if (!fb.isEmpty()) save = fb.get(0);
        }
        if (save == null) throw new NoSuchElementException("'Kaydet' butonu bulunamadı.");

        try { wait.until(ExpectedConditions.elementToBeClickable(save)).click(); }
        catch (Exception e) { jsClick(save); }
    }

    private void waitSidebarClosed() {
        waitMedium().until(d -> {
            List<WebElement> list = d.findElements(SIDEBAR_CONTENT);
            if (list.isEmpty()) return true;
            try {
                WebElement el = list.get(0);
                if (!el.isDisplayed()) return true;
                String style = el.getAttribute("style");
                if (style != null && (style.contains("display: none") || style.contains("opacity: 0"))) return true;
                String cls = el.getAttribute("class");
                return (cls != null && cls.contains("hidden"));
            } catch (StaleElementReferenceException e) {
                return true;
            }
        });
    }

    // ---------- Quick popup helpers ----------
    private String timeTextOfTile(WebElement tile) {
        try {
            WebElement details = tile.findElement(APPOINTMENT_DETAILS);
            for (WebElement s : details.findElements(By.cssSelector("span.text-xs"))) {
                String t = s.getText().trim();
                if (t.matches("\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2}")) return t;
            }
        } catch (Exception ignored) {}
        return null;
    }
    private boolean tileMatchesStart(WebElement tile, String hhmm, long startMs) {
        String tt = timeTextOfTile(tile);
        if (tt != null && tt.startsWith(hhmm)) return true;

        try {
            String aria = tile.getAttribute("aria-label");
            if (aria != null && aria.contains(hhmm + ":00")) return true;
        } catch (Exception ignored) {}

        try {
            String d = tile.getAttribute("data-top-start-duration");
            if (d != null && !d.isBlank()) {
                long val = Long.parseLong(d.trim());
                if (val == startMs) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
    private WebElement appointmentTileByTime(String hhmm, long startMs) {
        List<WebElement> preferred = driver.findElements(APPOINTMENT_TILE_BORDERED);
        List<WebElement> okPreferred = new ArrayList<>();
        for (WebElement t : preferred) {
            try {
                if (t.isDisplayed() && t.getRect().getHeight() > 0 && tileMatchesStart(t, hhmm, startMs))
                    okPreferred.add(t);
            } catch (Exception ignored) {}
        }
        if (!okPreferred.isEmpty()) {
            okPreferred.sort(Comparator.comparingInt(o -> {
                try {
                    String style = o.getAttribute("style");
                    int i = style.indexOf("top:");
                    if (i >= 0) {
                        String sub = style.substring(i).replaceAll("[^0-9]", " ").trim().split("\\s+")[0];
                        return Integer.parseInt(sub);
                    }
                } catch (Exception ignored) {}
                return 0;
            }));
            return okPreferred.get(okPreferred.size() - 1);
        }

        for (WebElement t : driver.findElements(APPOINTMENT_TILE_ANY)) {
            try {
                if (t.isDisplayed() && t.getRect().getHeight() > 0 && tileMatchesStart(t, hhmm, startMs))
                    return t;
            } catch (Exception ignored) {}
        }
        return null;
    }
    private WebElement appointmentTileAtLastSlotByDataAttr() {
        long startMs = startDurationMillis();
        By sel = By.cssSelector(
                "div.e-appointment.e-lib.e-draggable[role='button'][data-group-index='0']" +
                        "[data-top-start-duration='" + startMs + "']"
        );
        return waitLong().until(d -> {
            for (WebElement t : d.findElements(sel)) {
                try { if (t.isDisplayed() && t.getRect().getHeight() > 0) return t; }
                catch (Exception ignored) {}
            }
            return null;
        });
    }
    private void waitQuickPopupReady() {
        waitMedium().until(ExpectedConditions.visibilityOfElementLocated(QUICK_POPUP));
        waitMedium().until(d -> {
            try { for (WebElement h : d.findElements(QUICK_POPUP_HEADER)) if (h.isDisplayed()) return true; } catch (Exception ignored) {}
            try { WebElement f = d.findElement(APPOINTMENT_FOOTER); if (f.isDisplayed()) return true; } catch (Exception ignored) {}
            try { for (WebElement b : d.findElements(CHECKIN_BUTTON)) if (b.isDisplayed()) return true; } catch (Exception ignored) {}
            return false;
        });
    }
    private void openQuickPopupFromTile(WebElement tile) {
        scrollIntoView(tile);
        try {
            WebElement details = tile.findElement(APPOINTMENT_DETAILS);
            List<WebElement> icons = details.findElements(APPOINTMENT_EVENT_ICON);
            if (!icons.isEmpty() && icons.get(0).isDisplayed()) {
                try { waitShort().until(ExpectedConditions.elementToBeClickable(icons.get(0))).click(); }
                catch (Exception e) { jsClick(icons.get(0)); }
            } else {
                try { waitShort().until(ExpectedConditions.elementToBeClickable(tile)).click(); }
                catch (Exception e) { jsClick(tile); }
            }
        } catch (Exception e) {
            try { waitShort().until(ExpectedConditions.elementToBeClickable(tile)).click(); }
            catch (Exception ex) { jsClick(tile); }
        }
        try {
            waitQuickPopupReady();
        } catch (TimeoutException te) {
            try { new Actions(driver).moveToElement(tile).doubleClick().perform(); } catch (Exception ignored) {}
            waitQuickPopupReady();
        }
    }

    private String getStatusTextFromQuickPopup() {
        for (WebElement p : driver.findElements(QUICK_POPUP_STATUS)) {
            try {
                if (p.isDisplayed()) {
                    String t = p.getText();
                    if (t != null && !t.trim().isEmpty()) return t.trim();
                }
            } catch (StaleElementReferenceException ignored) {}
        }
        return null;
    }
    private boolean isStatusGeldiText(String t) {
        if (t == null) return false;
        String n = normalizeLike(t);
        return n.contains("geldi") || n.contains("tamamlandi");
    }
    private boolean isStatusBekliyorText(String t) {
        return t != null && normalizeLike(t).contains("bekliyor");
    }
    private WebElement visibleCheckinButton() {
        for (WebElement b : driver.findElements(CHECKIN_BUTTON)) {
            try { if (b.isDisplayed()) return b; } catch (Exception ignored) {}
        }
        try {
            return driver.findElement(By.xpath(
                    "//div[contains(@class,'e-quick-popup-wrapper')]//button[@data-testid='status-button' or " +
                            "normalize-space()='Check-in' or contains(translate(normalize-space(.),'İ','I'),'CHECK-IN')]"
            ));
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
    private boolean isDisabledButton(WebElement el) {
        try {
            if ("true".equalsIgnoreCase(el.getAttribute("disabled"))) return true;
            if ("true".equalsIgnoreCase(el.getAttribute("aria-disabled"))) return true;
            String cls = el.getAttribute("class");
            return cls != null && (cls.contains("disabled") || cls.contains("opacity-60") || cls.contains("cursor-not-allowed"));
        } catch (Exception ignored) {}
        return false;
    }
    private void closeQuickPopupIfOpen() {
        try {
            for (WebElement c : driver.findElements(QUICK_POPUP_CLOSE)) {
                if (c.isDisplayed()) {
                    try { c.click(); } catch (Exception e) { jsClick(c); }
                    waitTiny().until(ExpectedConditions.invisibilityOfElementLocated(QUICK_POPUP));
                    return;
                }
            }
        } catch (Exception ignored) {}
        try {
            WebElement backdrop = driver.findElement(By.cssSelector("body"));
            new Actions(driver).moveByOffset(5,5).click(backdrop).perform();
        } catch (Exception ignored) {}
        try { waitTiny().until(ExpectedConditions.invisibilityOfElementLocated(QUICK_POPUP)); } catch (Exception ignored) {}
    }

    private void assertStatusGeldi() {
        final long overallDeadline = System.currentTimeMillis() + 30_000;
        int reopenAttempts = 0;
        String lastSeen = null;

        while (System.currentTimeMillis() < overallDeadline) {
            try { waitTiny().until(ExpectedConditions.visibilityOfElementLocated(QUICK_POPUP)); }
            catch (TimeoutException te) { openQuickPopupFromSameSlot(); }

            long innerDeadline = System.currentTimeMillis() + 12_000;
            while (System.currentTimeMillis() < innerDeadline) {
                String t = getStatusTextFromQuickPopup();
                if (t != null) lastSeen = t;

                if (isStatusGeldiText(t)) return;

                if (isStatusBekliyorText(t)) {
                    WebElement btn = visibleCheckinButton();
                    if (btn != null && !isDisabledButton(btn)) {
                        try { waitShort().until(ExpectedConditions.elementToBeClickable(btn)).click(); }
                        catch (Exception e) { jsClick(btn); }
                    }
                    long afterDeadline = System.currentTimeMillis() + 8_000;
                    while (System.currentTimeMillis() < afterDeadline) {
                        String after = getStatusTextFromQuickPopup();
                        if (after != null) lastSeen = after;
                        if (isStatusGeldiText(after)) return;
                        try { Thread.sleep(400); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
                try { Thread.sleep(350); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
            closeQuickPopupIfOpen();
            openQuickPopupFromSameSlot();
            if (++reopenAttempts >= 3) break;
        }
        throw new TimeoutException("Randevu durumu 'Geldi/Tamamlandı' olmadı. Son görülen: " + lastSeen);
    }

    private void clickCheckInInQuickPopup() {
        waitQuickPopupReady();
        String s = getStatusTextFromQuickPopup();
        if (isStatusBekliyorText(s)) {
            WebElement btn = visibleCheckinButton();
            if (btn != null && !isDisabledButton(btn)) {
                try { waitShort().until(ExpectedConditions.elementToBeClickable(btn)).click(); }
                catch (Exception e) { jsClick(btn); }
            }
        }
        assertStatusGeldi();
    }

    private void openQuickPopupFromSameSlot() {
        final String hhmm = startTextHHmm();
        final long startMs = startDurationMillis();

        WebElement tile = null;
        try {
            tile = waitShort().until(d -> {
                WebElement t = appointmentTileByTime(hhmm, startMs);
                return (t != null && t.isDisplayed()) ? t : null;
            });
        } catch (TimeoutException ignored) {}

        if (tile == null) {
            try { tile = appointmentTileAtLastSlotByDataAttr(); } catch (TimeoutException ignored) {}
        }

        if (tile == null) {
            int idx = trIndexForHourDynamic(lastSlotHour, lastSlotFirstHalf);
            WebElement td = rowTdAt(idx);
            clickCellAndWaitSidebar(td);
            waitQuickPopupReady();
            return;
        }
        openQuickPopupFromTile(tile);
    }

    // ---------- Public E2E APIs ----------

    public void searchPatientInSidebar(String fullName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIDEBAR_TITLE));
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(SIDEBAR_CONTENT));

        WebElement input = wait.until(d -> {
            List<WebElement> a = d.findElements(PATIENT_SEARCH_INPUT);
            if (!a.isEmpty()) return a.get(0);
            List<WebElement> b = d.findElements(By.cssSelector("input[placeholder*='Ara'], input[placeholder='Ara']"));
            return b.isEmpty() ? null : b.get(0);
        });
        WebElement wrapper = input;
        try { wrapper = input.findElement(By.xpath("./ancestor::div[contains(@class,'justify-between')][1]")); }
        catch (NoSuchElementException ignored) {}
        try { wait.until(ExpectedConditions.elementToBeClickable(wrapper)).click(); }
        catch (Exception e) { jsClick(wrapper); }

        ((JavascriptExecutor) driver).executeScript(
                "const el=arguments[0];el.removeAttribute('disabled');el.setAttribute('aria-disabled','false');" +
                        "el.readOnly=false;el.className=(el.className||'').replace(/\\bopacity-60\\b|\\bcursor-not-allowed\\b|\\bdisabled\\b/g,'');",
                input
        );
        ((JavascriptExecutor) driver).executeScript(
                "const el=arguments[0], v=arguments[1]; el.focus(); el.value=''; el.dispatchEvent(new Event('input',{bubbles:true}));" +
                        "el.value=v; el.dispatchEvent(new Event('input',{bubbles:true})); el.dispatchEvent(new Event('change',{bubbles:true}));",
                input, fullName
        );
        try {
            String v = input.getAttribute("value");
            if (v == null || v.isBlank()) { input.click(); input.clear(); input.sendKeys(fullName); }
        } catch (Exception ignored) {}
        wait.until(d -> {
            try { return textLike(input.getAttribute("value"), fullName); }
            catch (StaleElementReferenceException e) { return false; }
        });

        clickSearchButton();
        waitResultsAppear();

        clickPatientResultItem(sidebar, fullName);
        clickSaveOnForm();
        waitSidebarClosed();

        openQuickPopupFromSameSlot();
        clickCheckInInQuickPopup();
    }

    public void openAppointmentDetailsOfLastSlot() {
        openQuickPopupFromSameSlot();
        waitQuickPopupReady();
    }

    public void deleteAppointmentOfLastSlot() {
        waitQuickPopupReady();

        WebElement deleteBtn = null;
        for (WebElement b : driver.findElements(QUICK_DELETE_BUTTON)) {
            try { if (b.isDisplayed()) { deleteBtn = b; break; } } catch (Exception ignored) {}
        }
        if (deleteBtn == null) {
            deleteBtn = driver.findElement(By.xpath(
                    "//div[contains(@class,'e-quick-popup-wrapper')]//button[@data-testid='appointment-delete-button' " +
                            "or normalize-space()='Sil' or contains(normalize-space(),'Delete')]"
            ));
        }
        try { waitShort().until(ExpectedConditions.elementToBeClickable(deleteBtn)).click(); }
        catch (Exception e) { jsClick(deleteBtn); }

        try {
            WebElement dialog = waitShort().until(d -> {
                for (WebElement x : d.findElements(MODAL_CONTAINER)) {
                    try { if (x.isDisplayed() && x.getRect().getHeight() > 0) return x; }
                    catch (Exception ignored) {}
                }
                return null;
            });
            if (dialog != null) {
                WebElement yes = null;
                for (WebElement b : dialog.findElements(MODAL_YES_TEXT)) { if (b.isDisplayed()) { yes = b; break; } }
                if (yes == null) {
                    for (WebElement b : dialog.findElements(MODAL_PRIMARY_IN_FOOTER)) {
                        try {
                            if (b.isDisplayed() && !"okay-button".equalsIgnoreCase(b.getAttribute("id"))) { yes = b; break; }
                        } catch (Exception ignored) {}
                    }
                }
                if (yes != null) {
                    try { waitShort().until(ExpectedConditions.elementToBeClickable(yes)).click(); }
                    catch (Exception e) { jsClick(yes); }
                }
            }
        } catch (TimeoutException ignored) { }

        try {
            WebElement reasonDialog = waitMedium().until(d -> {
                List<WebElement> ds = d.findElements(MODAL_CONTAINER);
                for (int i = ds.size() - 1; i >= 0; i--) {
                    WebElement x = ds.get(i);
                    try {
                        boolean hasOkayId = !x.findElements(MODAL_OKAY_BUTTON).isEmpty();
                        boolean hasOkayText = !x.findElements(MODAL_OKAY_TEXT).isEmpty();
                        if (x.isDisplayed() && (hasOkayId || hasOkayText)) return x;
                    } catch (Exception ignored) {}
                }
                return null;
            });
            if (reasonDialog != null) {
                WebElement ok = null;
                for (WebElement b : reasonDialog.findElements(MODAL_OKAY_BUTTON)) { if (b.isDisplayed()) { ok = b; break; } }
                if (ok == null) {
                    for (WebElement b : reasonDialog.findElements(MODAL_OKAY_TEXT)) { if (b.isDisplayed()) { ok = b; break; } }
                }
                if (ok == null) {
                    for (WebElement b : reasonDialog.findElements(By.xpath(".//button[contains(.,'Tamam') or contains(.,'OK')]"))) {
                        if (b.isDisplayed()) { ok = b; break; }
                    }
                }
                if (ok != null) {
                    try { waitShort().until(ExpectedConditions.elementToBeClickable(ok)).click(); }
                    catch (Exception e) { jsClick(ok); }
                }
            }
        } catch (TimeoutException ignored) { }

        final long startMs = startDurationMillis();
        final By tileBy = By.cssSelector(
                "div.e-appointment.e-lib.e-draggable[role='button'][data-group-index='0']" +
                        "[data-top-start-duration='" + startMs + "']"
        );
        waitLong().until(d -> {
            try {
                for (WebElement t : d.findElements(tileBy)) {
                    try { if (t.isDisplayed() && t.getRect().getHeight() > 0) return false; }
                    catch (Exception ignored) {}
                }
                return true;
            } catch (Exception ex) { return true; }
        });
        try { waitShort().until(ExpectedConditions.invisibilityOfElementLocated(QUICK_POPUP)); } catch (Exception ignored) {}
    }

    /** HH:00 veya HH:30 formatındaki gerçek zamanı hedef slot hücresine tıkla.
     *  Önce hücreyi doğrudan (aria-label/metin) bulur; bulunamazsa dinamik satır-indeks fallback’i kullanır. */
    public void clickSlotAt(int hour24, int minute) {
        if (minute != 0 && minute != 30)
            throw new IllegalArgumentException("Dakika sadece 0 veya 30 olabilir. Verilen: " + minute);
        if (hour24 < 0 || hour24 > 23)
            throw new IllegalArgumentException("Saat 0..23 olmalı. Verilen: " + hour24);

        WebElement cell = findCellByTimeScrolling(hour24, minute);
        if (cell != null) {
            this.lastSlotHour = hour24;
            this.lastSlotFirstHalf = (minute == 0);
            scrollIntoView(cell);
            clickCellAndWaitSidebar(cell);
            return;
        }
        clickSlot(hour24, minute == 0);
    }
}
