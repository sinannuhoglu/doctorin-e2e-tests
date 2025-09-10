package com.sinannuhoglu.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DriverFactory
 * <p>
 * Tek noktadan WebDriver üretimi (lokal ChromeDriver veya Grid/Remote).
 * Ayarlar: System properties → config → güvenli varsayılanlar.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver create() {
        ConfigReader cfg = ConfigReader.fromEnvironment();

        String browser = firstNonBlank(
                System.getProperty("browser"),
                cfg.get("selenium.browser"),
                "chrome"
        ).toLowerCase(Locale.ROOT);

        boolean fastMode = pickBoolean("fastMode", cfg.getBoolean("selenium.fastMode", false));
        boolean headless = pickBoolean("headless", cfg.getBoolean("selenium.headless", false));
        boolean disableImages = pickBoolean("disableImages", cfg.getBoolean("selenium.disableImages", fastMode));

        String pls = firstNonBlank(
                System.getProperty("pageLoadStrategy"),
                cfg.get("selenium.pageLoadStrategy"),
                "eager"
        );
        PageLoadStrategy pageLoadStrategy = switch (pls.toLowerCase(Locale.ROOT)) {
            case "eager" -> PageLoadStrategy.EAGER;
            case "none"  -> PageLoadStrategy.NONE;
            default      -> PageLoadStrategy.NORMAL;
        };

        Dimension windowSize = parseWindowSize(firstNonBlank(
                System.getProperty("windowSize"),
                cfg.get("selenium.windowSize"),
                "1440x900"
        ));

        long pageLoadTimeoutSec = pickLong(
                "pageLoadTimeoutSec",
                cfg.getLong("selenium.pageLoadTimeoutSec", fastMode ? 45 : 120)
        );

        boolean gridEnabled = cfg.getBoolean("selenium.grid.enabled", false);
        String gridUrl = firstNonBlank(System.getProperty("gridUrl"), cfg.get("selenium.grid.url"));

        if (!"chrome".equals(browser)) {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        ChromeOptions opts = new ChromeOptions();
        opts.setPageLoadStrategy(pageLoadStrategy);
        opts.setAcceptInsecureCerts(true);
        opts.addArguments(
                "--disable-notifications",
                "--disable-popup-blocking",
                "--remote-allow-origins=*",
                "--no-sandbox",
                "--disable-extensions",
                "--lang=tr-TR",
                "--window-size=" + windowSize.width + "," + windowSize.height,
                "--disable-renderer-backgrounding",
                "--disable-background-timer-throttling",
                "--disable-backgrounding-occluded-windows",
                "--disable-features=TranslateUI,BackForwardCache",
                "--dns-prefetch-disable",
                "--no-proxy-server"
        );

        if (headless) {
            opts.addArguments("--headless=new", "--disable-gpu");
        }

        if (disableImages) {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.managed_default_content_settings.images", 2);
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            opts.setExperimentalOption("prefs", prefs);
        }

        WebDriver driver = (gridEnabled && nonBlank(gridUrl))
                ? new RemoteWebDriver(toUrl(gridUrl), opts)
                : createLocalChrome(opts);

        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeoutSec));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(fastMode ? 15 : 30));
        try { driver.manage().window().setSize(windowSize); } catch (Exception ignored) {}

        return driver;
    }

    private static WebDriver createLocalChrome(ChromeOptions opts) {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(opts);
    }

    private static URL toUrl(String s) {
        try { return new URL(s); }
        catch (MalformedURLException e) { throw new IllegalArgumentException("Geçersiz Grid URL: " + s, e); }
    }

    private static boolean nonBlank(String s) { return s != null && !s.trim().isEmpty(); }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (nonBlank(v)) return v.trim();
        return null;
    }

    private static boolean pickBoolean(String sysKey, boolean fallback) {
        String v = System.getProperty(sysKey);
        if (!nonBlank(v)) return fallback;
        return switch (v.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> fallback;
        };
    }

    private static long pickLong(String sysKey, long fallback) {
        String v = System.getProperty(sysKey);
        if (!nonBlank(v)) return fallback;
        try { return Long.parseLong(v.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    private static Dimension parseWindowSize(String raw) {
        if (!nonBlank(raw)) return new Dimension(1440, 900);
        String s = raw.trim().toLowerCase(Locale.ROOT).replace('*', 'x').replace(',', 'x');
        String[] parts = s.split("x");
        try {
            int w = Integer.parseInt(parts[0].trim());
            int h = Integer.parseInt(parts[1].trim());
            return new Dimension(w, h);
        } catch (Exception ignore) {
            return new Dimension(1440, 900);
        }
    }
}
