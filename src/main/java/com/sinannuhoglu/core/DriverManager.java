package com.sinannuhoglu.core;

import org.openqa.selenium.WebDriver;

import java.util.Objects;

/**
 * ThreadLocal tabanlı WebDriver saklayıcısı.
 * Paralel testler için her thread'e özel sürücü sunar.
 * TestContext'ten bağımsızdır; set edilmeden get çağrılırsa hata fırlatır.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();

    private DriverManager() {}

    /** Aktif thread için WebDriver kaydeder. */
    public static void setDriver(WebDriver driver) {
        TL.set(Objects.requireNonNull(driver, "driver"));
    }

    /** Aktif thread'deki WebDriver'ı döndürür; yoksa IllegalStateException atar. */
    public static WebDriver getDriver() {
        WebDriver d = TL.get();
        if (d == null) {
            throw new IllegalStateException(
                    "WebDriver henüz ayarlanmamış. Test öncesi Hook içinde DriverManager.setDriver(driver) çağırın."
            );
        }
        return d;
    }

    /** Aktif thread’de WebDriver set edilmiş mi? */
    public static boolean isSet() {
        return TL.get() != null;
    }

    /** Aktif thread’deki referansı temizler (quit çağırmaz). */
    public static void removeDriver() {
        TL.remove();
    }

    /** Varsa WebDriver.quit() çağırır ve ThreadLocal’ı temizler (hataları yutar). */
    public static void quitAndRemoveQuietly() {
        WebDriver d = TL.get();
        try {
            if (d != null) d.quit();
        } catch (Throwable ignored) {

        } finally {
            TL.remove();
        }
    }
}
