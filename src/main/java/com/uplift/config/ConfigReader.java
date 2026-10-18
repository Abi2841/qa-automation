package com.uplift.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration reader for the test framework.
 *
 * Implements a layered configuration resolution strategy:
 * 1. Java System Properties (e.g., -Dbrowser=edge -Dheadless=false)
 * 2. Operating System Environment Variables (e.g., QA_BASE_URL, API_AUTH_TOKEN)
 * 3. Optional local secrets.properties file (gitignored, for local credentials)
 * 4. Default config.properties file on the test classpath
 */
public final class ConfigReader {

    private static final Properties properties = new Properties();
    private static final Properties secrets = new Properties();
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String SECRETS_FILE = "secrets.properties";

    static {
        loadProperties();
        loadSecrets();
    }

    private ConfigReader() {
        // Prevent direct instantiation
    }

    private static void loadProperties() {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("[WARN] " + DEFAULT_CONFIG_FILE + " not found on classpath.");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load " + DEFAULT_CONFIG_FILE + ": " + e.getMessage());
        }
    }

    private static void loadSecrets() {
        // First try to load from classpath
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(SECRETS_FILE)) {
            if (input != null) {
                secrets.load(input);
                return;
            }
        } catch (IOException ignored) {
            // secrets file is optional
        }

        // Second fallback: check filesystem in project root or test resources
        File localSecrets = new File("src/test/resources/" + SECRETS_FILE);
        if (localSecrets.exists()) {
            try (FileInputStream fis = new FileInputStream(localSecrets)) {
                secrets.load(fis);
            } catch (IOException ignored) {
                // optional
            }
        }
    }

    /**
     * Resolves a configuration property value with priority:
     * System Property > Environment Variable > secrets.properties > config.properties
     */
    public static String getProperty(String key, String defaultValue) {
        // 1. System property
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.trim().isEmpty()) {
            return systemProperty.trim();
        }

        // 2. Environment variable (check exact and uppercase with underscores)
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        // 3. Secrets property file (for passwords/tokens)
        String secretValue = secrets.getProperty(key);
        if (secretValue != null && !secretValue.trim().isEmpty()) {
            return secretValue.trim();
        }

        // 4. Default config.properties
        String propertyValue = properties.getProperty(key);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }

        return defaultValue;
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Retrieves sensitive secrets (passwords, auth tokens).
     */
    public static String getSecret(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    public static String getSecret(String key) {
        return getProperty(key, "");
    }

    // Typed configuration accessors
    public static String getBaseUrl() {
        return getProperty("base.url", "https://the-internet.herokuapp.com");
    }

    public static String getAdminPortalUrl() {
        return getProperty("admin.portal.url", getBaseUrl());
    }

    public static String getEmployerPortalUrl() {
        return getProperty("employer.portal.url", getBaseUrl());
    }

    public static String getAdminUsername() {
        return getSecret("admin.username", "super_admin_user");
    }

    public static String getAdminPassword() {
        return getSecret("admin.password", "SuperAdminSecretPassword123!");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url", "https://reqres.in/api");
    }

    public static String getBrowser() {
        return getProperty("browser", "chrome").toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "true"));
    }

    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait", "10"));
    }

    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("explicit.wait", "15"));
    }

    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page.load.timeout", "30"));
    }

    public static boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }

    public static String getReportPath() {
        return getProperty("report.path", "reports/ExtentReport.html");
    }
}
