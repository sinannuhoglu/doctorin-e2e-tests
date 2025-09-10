package com.sinannuhoglu.util;

import com.sinannuhoglu.core.ConfigReader;

/** Basit uygulama konfig yardımcıları. */
public final class AppConfig {

    private AppConfig() {}

    /**
     * baseUrl çözümleme önceliği:
     * 1) -DbaseUrl
     * 2) config: app.baseUrl
     * 3) legacy: baseUrl
     */
    public static String baseUrl() {
        String fromSys = System.getProperty("baseUrl");
        if (nonBlank(fromSys)) return fromSys.trim();

        ConfigReader cfg = ConfigReader.fromEnvironment();
        String url = firstNonBlank(cfg.get("app.baseUrl"), cfg.get("baseUrl"));
        if (!nonBlank(url)) {
            throw new IllegalStateException(
                    "baseUrl tanımlı değil.\n" +
                            "Çözüm: -DbaseUrl=https://<login-url> verin ya da resources/config/" +
                            cfg.env() + ".properties içine app.baseUrl=... ekleyin."
            );
        }
        return url.trim();
    }

    private static boolean nonBlank(String s) { return s != null && !s.isBlank(); }
    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) if (nonBlank(v)) return v;
        return null;
    }
}
