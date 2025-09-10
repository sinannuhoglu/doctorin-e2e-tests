package com.sinannuhoglu.core;

import org.openqa.selenium.WebDriver;

import java.util.Objects;

/**
 * Senaryo süresince WebDriver ve ConfigReader yaşam döngüsünü yöneten,
 * tekil (singleton) bağlam.
 * <p>
 * Davranış akışı:
 * <ol>
 *   <li>{@link #init()} çağrısında etkin ortamdan {@link ConfigReader} yüklenir,</li>
 *   <li>gerekli Selenium ayarları {@code System.setProperty} ile yayılır,</li>
 *   <li>{@link DriverFactory} ile {@link WebDriver} oluşturulur ve
 *       {@link DriverManager} üzerinden aktif iş parçacığına atanır.</li>
 * </ol>
 * {@link #quit()} çağrısı, sürücüyü kapatıp {@link DriverManager} ThreadLocal’ını temizler.
 */
public final class TestContext {

    private static final TestContext INSTANCE = new TestContext();

    private WebDriver driver;
    private ConfigReader cfg;

    private TestContext() { }

    /** Tekil örnek erişimi. */
    public static TestContext get() {
        return INSTANCE;
    }

    /**
     * Bağlamı başlatır (idempotent).
     * <ul>
     *   <li>ConfigReader’ı ortamdan yükler,</li>
     *   <li>önemli Selenium ayarlarını System property’lere taşır,</li>
     *   <li>WebDriver oluşturup DriverManager’a kaydeder.</li>
     * </ul>
     */
    public synchronized void init() {
        if (driver != null) return;

        this.cfg = ConfigReader.fromEnvironment();
        propagateSeleniumProps(cfg);
        propagateBaseUrl(cfg);

        this.driver = DriverFactory.create();
        DriverManager.setDriver(this.driver);

        System.out.println("[TestContext] Initialized. env=" + cfg.env());
    }

    /**
     * Dışarıda oluşturulmuş bir WebDriver’ı bağlar.
     * Mevcut bir sürücü varsa değiştirmez; önce {@link #quit()} çağrılmalıdır.
     */
    public synchronized void setDriver(WebDriver externalDriver) {
        Objects.requireNonNull(externalDriver, "externalDriver");
        if (this.driver != null) return;
        this.driver = externalDriver;
        DriverManager.setDriver(this.driver);
    }

    /** Başlatılmış mı? */
    public synchronized boolean isInitialized() {
        return this.driver != null;
    }

    /** Aktif WebDriver (init sonrası non-null). */
    public WebDriver driver() { return driver; }

    /** Aktif ConfigReader (init sonrası non-null). */
    public ConfigReader cfg() { return cfg; }

    /** Sürücüyü kapatır, ThreadLocal’ı ve konfigürasyonu temizler. */
    public synchronized void quit() {
        try {
            if (driver != null) driver.quit();
        } catch (Exception ignored) {
        } finally {
            DriverManager.removeDriver();
            driver = null;
            cfg = null;
            System.out.println("[TestContext] Quit.");
        }
    }

    // ---------------------------------------------------------------------
    // İç yardımcılar
    // ---------------------------------------------------------------------

    /**
     * Config’ten okunan kritik Selenium ayarlarını System property’lere taşır.
     * Haritalama:
     * <pre>
     *   selenium.browser            → browser            (default: chrome)
     *   selenium.headless           → headless           (default: false)
     *   selenium.disableImages      → disableImages      (default: true)
     *   selenium.pageLoadTimeoutSec → pageLoadTimeoutSec (yoksa selenium.timeoutSec → 60)
     * </pre>
     */
    private static void propagateSeleniumProps(ConfigReader cfg) {
        setIfAbsent("browser", cfg.get("selenium.browser", "chrome"));
        setIfAbsent("headless", String.valueOf(cfg.getBoolean("selenium.headless", false)));
        setIfAbsent("disableImages", String.valueOf(cfg.getBoolean("selenium.disableImages", true)));

        long plt = cfg.getLong("selenium.pageLoadTimeoutSec",
                cfg.getLong("selenium.timeoutSec", 60L));
        setIfAbsent("pageLoadTimeoutSec", String.valueOf(plt));
    }

    /** AppConfig uyumluluğu için baseUrl’i System property olarak yayımlar. */
    private static void propagateBaseUrl(ConfigReader cfg) {
        String baseUrl = firstNonBlank(
                System.getProperty("baseUrl"),
                cfg.get("baseUrl"),
                cfg.get("app.baseUrl")
        );
        if (nonBlank(baseUrl)) {
            System.setProperty("baseUrl", baseUrl);
        }
    }

    private static void setIfAbsent(String key, String value) {
        if (!nonBlank(key) || value == null) return;
        if (!nonBlank(System.getProperty(key))) {
            System.setProperty(key, value);
        }
    }

    private static boolean nonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (nonBlank(v)) return v.trim();
        }
        return null;
    }
}
