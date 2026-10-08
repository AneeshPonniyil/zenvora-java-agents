package com.zenvora.agents.common;

/**
 * Reads configuration from environment variables. Secrets (API keys) must only ever
 * come from the environment, never from source code or committed files.
 */
public final class AppConfig {

    private AppConfig() {}

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static String require(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    /** Mock mode is ON unless DEMO_MOCK=false, so nothing calls a paid model by accident. */
    public static boolean mockMode() {
        return !"false".equalsIgnoreCase(get("DEMO_MOCK", "true"));
    }
}
