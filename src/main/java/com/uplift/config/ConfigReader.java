package com.uplift.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration reader for the test framework.
 *
 * Implements a layered configuration resolution strategy (first non-empty value wins):
 * 1. Java System Properties (e.g., -Dbrowser=edge -Dheadless=false)
 * 2. Operating System Environment Variables (admin.password -> ADMIN_PASSWORD), used by Jenkins credentials
 * 3. Optional local secrets.properties file (gitignored, for local credentials)
 * 4. environments/&lt;env&gt;.properties selected with -Denv=qa|demo (default qa)
 * 5. Common config.properties file on the test classpath
 *
 * No credentials have defaults in code: required values that are missing fail fast with a clear message.
 */
public final class ConfigReader {

    private static final Properties properties = new Properties();
    private static final Properties secrets = new Properties();
    private static final String COMMON_CONFIG_FILE = "config.properties";
    private static final String ENVIRONMENTS_DIR = "environments/";
    private static final String SECRETS_FILE = "secrets.properties";
    private static final String DEFAULT_ENVIRONMENT = "qa";

    static {
        loadProperties();
        loadSecrets();
    }

    private ConfigReader() {
        // Prevent direct instantiation
    }

    private static void loadProperties() {
        if (!loadClasspathFile(COMMON_CONFIG_FILE, properties)) {
            System.err.println("[WARN] " + COMMON_CONFIG_FILE + " not found on classpath.");
        }
        // Loaded second so environment values override the common defaults
        String envFile = ENVIRONMENTS_DIR + getEnvironment() + ".properties";
        if (!loadClasspathFile(envFile, properties)) {
            throw new IllegalStateException("Unknown environment '" + getEnvironment()
                    + "': expected src/test/resources/" + envFile);
        }
    }

    private static boolean loadClasspathFile(String resource, Properties target) {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                return false;
            }
            target.load(input);
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resource + ": " + e.getMessage(), e);
        }
    }

    private static void loadSecrets() {
        // First try to load from classpath
        if (loadClasspathFile(SECRETS_FILE, secrets)) {
            return;
        }

        // Second fallback: check filesystem in test resources
        File localSecrets = new File("src/test/resources/" + SECRETS_FILE);
        if (localSecrets.exists()) {
            try (FileInputStream fis = new FileInputStream(localSecrets)) {
                secrets.load(fis);
            } catch (IOException ignored) {
                // secrets file is optional
            }
        }
    }

    /**
     * Active environment name: -Denv=..., else TEST_ENV environment variable, else "qa".
     */
    public static String getEnvironment() {
        String env = System.getProperty("env");
        if (env == null || env.trim().isEmpty()) {
            env = System.getenv("TEST_ENV");
        }
        return (env == null || env.trim().isEmpty()) ? DEFAULT_ENVIRONMENT : env.trim().toLowerCase();
    }

    /**
     * Resolves a configuration property value with priority:
     * System Property > Environment Variable > secrets.properties > environment file > config.properties
     */
    public static String getProperty(String key, String defaultValue) {
        // 1. System property
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.trim().isEmpty()) {
            return systemProperty.trim();
        }

        // 2. Environment variable (uppercase with underscores)
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

        // 4/5. Environment file layered over config.properties
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
     * Returns a value that the current test cannot run without, or throws an explanatory error.
     */
    public static String getRequired(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required configuration '" + key + "' for environment '"
                    + getEnvironment() + "'. Provide it with -D" + key + "=..., environment variable "
                    + key.toUpperCase().replace('.', '_') + ", src/test/resources/secrets.properties "
                    + "(see secrets.properties.example) or environments/" + getEnvironment() + ".properties.");
        }
        return value;
    }

    public static boolean isSet(String key) {
        return getProperty(key) != null;
    }

    private static int getInt(String key, int defaultValue) {
        return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
    }

    // Typed configuration accessors
    public static String getBaseUrl() {
        return getRequired("base.url");
    }

    public static String getAdminPortalUrl() {
        return getRequired("admin.portal.url");
    }

    public static String getEmployerPortalUrl() {
        return getRequired("employer.portal.url");
    }

    public static String getAdminUsername() {
        return getRequired("admin.username");
    }

    public static String getAdminPassword() {
        return getRequired("admin.password");
    }

    public static String getApiBaseUrl() {
        return getRequired("api.base.url");
    }

    public static String getBrowser() {
        return getProperty("browser", "chrome").toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "true"));
    }

    public static int getImplicitWait() {
        return getInt("implicit.wait", 0);
    }

    public static int getExplicitWait() {
        return getInt("explicit.wait", 20);
    }

    public static int getShortWait() {
        return getInt("short.wait", 5);
    }

    public static int getPageLoadTimeout() {
        return getInt("page.load.timeout", 60);
    }

    public static boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }

    public static String getReportPath() {
        return getProperty("report.path", "reports/ExtentReport.html");
    }

    public static String getTestDataPrefix() {
        return getProperty("test.data.prefix", "QA-AUTO-");
    }
}
