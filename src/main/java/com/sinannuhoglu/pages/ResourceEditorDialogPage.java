package com.sinannuhoglu.pages;

import com.sinannuhoglu.core.BasePage;
import com.sinannuhoglu.core.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Resource editor dialog: Workplan tab ve gün etkileşimleri. */
public class ResourceEditorDialogPage extends BasePage {

    private final By workplanTab = By.xpath("//a[contains(.,'Workplan')]");
    private final By panel = By.cssSelector(".workplan-management");
    private final String dayXpath = "//div[contains(@class,'day-label') and normalize-space(text())='%s']";

    public ResourceEditorDialogPage(WebDriver driver, ConfigReader cfg) { super(driver, cfg); }
    public ResourceEditorDialogPage(WebDriver driver) { super(driver, 20); }

    /** Workplan sekmesini açar ve paneli bekler. */
    public void openWorkplanTab() {
        WebElement tab = waitClickable(workplanTab);
        jsClick(tab);
        waitVisible(panel);
    }

    /** Haftalık görünümde belirtilen günü açar. */
    public void openWorkplanForDay(String dayTr) {
        By dayLocator = By.xpath(String.format(dayXpath, dayTr));
        WebElement day = waitClickable(dayLocator);
        jsClick(day);
    }

    /** Workplan paneli görünür mü? */
    public boolean isPanelVisible() {
        return isPresent(panel);
    }
}
