package com.sinannuhoglu.core;

import org.openqa.selenium.WebDriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * Basit ve esnek Page Object fabrikası.
 * Öncelik sırasıyla şu yapıcı imzalarını dener:
 * (WebDriver, ConfigReader) → (WebDriver) → (ConfigReader) → ().
 * WebDriver/ConfigReader varsayılan olarak TestContext/DriverManager ve
 * ConfigReader.fromEnvironment() ile çözülür.
 */
public final class PageFactory {

    private PageFactory() {}

    /** Varsayılan bağlamı (driver + config) kullanarak örnek üretir. */
    public static <T> T create(Class<T> pageClass) {
        Objects.requireNonNull(pageClass, "pageClass");
        WebDriver driver = resolveDriver();
        ConfigReader cfg = resolveConfig();
        return instantiate(pageClass, driver, cfg);
    }

    /** Belirtilen bağlamla (driver + config) örnek üretir. */
    public static <T> T create(Class<T> pageClass, WebDriver driver, ConfigReader cfg) {
        Objects.requireNonNull(pageClass, "pageClass");
        return instantiate(pageClass, driver, cfg);
    }

    // ----------------------------------------------------------------

    private static WebDriver resolveDriver() {
        try {
            TestContext ctx = TestContext.get();
            if (ctx != null && ctx.driver() != null) return ctx.driver();
        } catch (Throwable ignored) {}
        try {
            if (DriverManager.isSet()) return DriverManager.getDriver();
        } catch (Throwable ignored) {}
        return null;
    }

    private static ConfigReader resolveConfig() {
        try {
            TestContext ctx = TestContext.get();
            if (ctx != null && ctx.cfg() != null) return ctx.cfg();
        } catch (Throwable ignored) {}
        try {
            return ConfigReader.fromEnvironment();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static <T> T instantiate(Class<T> pageClass, WebDriver driver, ConfigReader cfg) {
        Constructor<T> c;

        c = ctor(pageClass, WebDriver.class, ConfigReader.class);
        if (c != null) {
            require("WebDriver", driver, c);
            return newInstance(c, driver, cfg);
        }

        c = ctor(pageClass, WebDriver.class);
        if (c != null) {
            require("WebDriver", driver, c);
            return newInstance(c, driver);
        }

        c = ctor(pageClass, ConfigReader.class);
        if (c != null) {
            require("ConfigReader", cfg, c);
            return newInstance(c, cfg);
        }

        c = ctor(pageClass);
        if (c != null) return newInstance(c);

        String available = listConstructors(pageClass);
        throw new IllegalStateException(
                "Uygun yapıcı bulunamadı: " + pageClass.getName() +
                        " | Aranan: (WebDriver,ConfigReader),(WebDriver),(ConfigReader),()" +
                        " | Mevcut: " + available
        );
    }

    private static void require(String name, Object value, Constructor<?> c) {
        if (value == null) {
            throw new IllegalStateException(
                    name + " gerekli ancak bulunamadı. Yapıcı: " + signature(c) +
                            " | Çözüm: Hook içinde DriverManager.setDriver(driver)/ConfigReader kurun " +
                            "ya da PageFactory.create(Page, driver, cfg) kullanın."
            );
        }
    }

    private static <T> Constructor<T> ctor(Class<T> type, Class<?>... params) {
        try {
            Constructor<T> c = type.getDeclaredConstructor(params);
            if (!Modifier.isPublic(c.getModifiers())) c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Constructor<?> c, Object... args) {
        try {
            return (T) c.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Nesne oluşturulamadı: " + signature(c), e);
        }
    }

    private static String listConstructors(Class<?> type) {
        Constructor<?>[] all = type.getDeclaredConstructors();
        if (all.length == 0) return "yok";
        return Arrays.stream(all).map(PageFactory::signature).reduce((a, b) -> a + ", " + b).orElse("yok");
    }

    private static String signature(Constructor<?> c) {
        String params = Arrays.stream(c.getParameterTypes())
                .map(Class::getSimpleName)
                .reduce((a, b) -> a + "," + b).orElse("");
        return c.getDeclaringClass().getSimpleName() + "(" + params + ")";
    }
}
