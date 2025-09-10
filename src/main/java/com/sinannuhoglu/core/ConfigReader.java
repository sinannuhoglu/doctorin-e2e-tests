package com.sinannuhoglu.core;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConfigReader
 * <p>
 * Tek noktadan konfigürasyon erişimi. Öncelik sırası:
 * System properties → resources/config/default.properties →
 * resources/config/&lt;env&gt;.properties → Environment variables (FOO_BAR).
 */
public class ConfigReader {

    private static final String DEFAULT_ENV  = "test";
    private static final String DEFAULT_FILE = "config/default.properties";
    private static final Pattern SIMPLE_DURATION =
            Pattern.compile("^\\s*(\\d+)\\s*(ms|s|m|h|d)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static final int MAX_PLACEHOLDER_DEPTH = 25;

    private final String env;
    private final Properties props = new Properties();

    public static ConfigReader fromEnvironment() {
        String e = System.getProperty("env");
        if (e == null || e.isBlank()) e = System.getenv("ENV");
        if (e == null || e.isBlank()) e = DEFAULT_ENV;
        return new ConfigReader(e);
    }

    public ConfigReader(String env) {
        this.env = (env == null || env.isBlank()) ? DEFAULT_ENV : env.trim();
        loadInto(props, DEFAULT_FILE, false);
        loadInto(props, "config/" + this.env + ".properties", true);
    }

    private static void loadInto(Properties target, String resourcePath, boolean required) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) {
                if (required) throw new IllegalStateException("Config not found: /" + resourcePath);
                return;
            }
            target.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Config load failed: /" + resourcePath, e);
        }
    }

    public String get(String key) {
        String raw = rawGet(key);
        return nonBlank(raw) ? resolvePlaceholders(raw) : null;
    }

    public String get(String key, String def) {
        String v = get(key);
        return nonBlank(v) ? v : def;
    }

    public String getRequired(String key) {
        String v = get(key);
        if (!nonBlank(v)) throw new IllegalStateException("Required config missing: " + key);
        return v;
    }

    public int getInt(String key, int def) {
        String v = get(key);
        if (!nonBlank(v)) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid int for '" + key + "': " + v, e); }
    }

    public long getLong(String key, long def) {
        String v = get(key);
        if (!nonBlank(v)) return def;
        try { return Long.parseLong(v.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid long for '" + key + "': " + v, e); }
    }

    public double getDouble(String key, double def) {
        String v = get(key);
        if (!nonBlank(v)) return def;
        try { return Double.parseDouble(v.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid double for '" + key + "': " + v, e); }
    }

    public boolean getBoolean(String key, boolean def) {
        String v = get(key);
        if (!nonBlank(v)) return def;
        String s = v.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "true": case "1": case "yes": case "y": case "on":  return true;
            case "false": case "0": case "no": case "n": case "off": return false;
            default: throw new IllegalArgumentException("Invalid boolean for '" + key + "': " + v);
        }
    }

    public Duration getDuration(String key, Duration def) {
        String v = get(key);
        if (!nonBlank(v)) return def;
        return parseDuration(v.trim(), key);
    }

    public List<String> getList(String key) {
        String v = get(key);
        if (!nonBlank(v)) return Collections.emptyList();
        String[] tokens = v.split("[,;]");
        List<String> out = new ArrayList<>(tokens.length);
        for (String t : tokens) {
            String s = t.trim();
            if (!s.isEmpty()) out.add(s);
        }
        return Collections.unmodifiableList(out);
    }

    public Map<String, String> byPrefix(String prefix, boolean stripPrefix) {
        Objects.requireNonNull(prefix, "prefix");
        Map<String, String> map = new LinkedHashMap<>();
        Set<String> keys = new LinkedHashSet<>();
        props.stringPropertyNames().forEach(keys::add);
        System.getProperties().stringPropertyNames().forEach(keys::add);

        for (String k : keys) {
            if (k.startsWith(prefix)) {
                String v = get(k);
                if (v != null) {
                    String mk = stripPrefix ? k.substring(prefix.length()) : k;
                    map.put(mk, v);
                }
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public String env() { return env; }

    @Override
    public String toString() {
        return "ConfigReader{env='" + env + "', sources=[" +
                DEFAULT_FILE + ", config/" + env + ".properties, system, env]}";
    }

    private String rawGet(String key) {
        String val = System.getProperty(key);
        if (nonBlank(val)) return val;

        val = props.getProperty(key);
        if (nonBlank(val)) return val;

        String envKey = key.replace('.', '_').toUpperCase(Locale.ROOT);
        val = System.getenv(envKey);
        return nonBlank(val) ? val : null;
    }

    private String resolvePlaceholders(String input) {
        return resolvePlaceholders(input, new HashSet<>(), 0);
    }

    private String resolvePlaceholders(String input, Set<String> chain, int depth) {
        if (input == null || input.indexOf('$') < 0) return input;
        if (depth > MAX_PLACEHOLDER_DEPTH) {
            throw new IllegalStateException("Placeholder resolution too deep (possible cycle): " + input);
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER.matcher(input);
        int last = 0;

        while (matcher.find()) {
            sb.append(input, last, matcher.start());
            String key = matcher.group(1);

            if (!chain.add(key)) {
                throw new IllegalStateException("Circular placeholder detected: " + chain + " → " + key);
            }

            String repRaw = rawGet(key);
            if (repRaw == null) {
                throw new IllegalStateException("Unresolved placeholder: ${" + key + "}");
            }

            String rep = resolvePlaceholders(repRaw, chain, depth + 1);
            chain.remove(key);

            sb.append(rep);
            last = matcher.end();
        }
        sb.append(input, last, input.length());
        String out = sb.toString();

        return (out.indexOf('$') >= 0) ? resolvePlaceholders(out, chain, depth + 1) : out;
    }

    private static boolean nonBlank(String s) { return s != null && !s.trim().isEmpty(); }

    private static Duration parseDuration(String value, String keyForError) {
        Matcher m = SIMPLE_DURATION.matcher(value);
        if (m.matches()) {
            long num = Long.parseLong(m.group(1));
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            switch (unit) {
                case "ms": return Duration.ofMillis(num);
                case "s":  return Duration.ofSeconds(num);
                case "m":  return Duration.ofMinutes(num);
                case "h":  return Duration.ofHours(num);
                case "d":  return Duration.ofDays(num);
                default:   throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        }
        try { return Duration.parse(value); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid duration for '" + keyForError + "': " + value, e); }
    }
}
