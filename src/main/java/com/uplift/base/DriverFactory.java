package com.uplift.base;

import com.uplift.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * ThreadSafe WebDriver factory to initialize, retrieve, and quit browser instances.
 * Utilizes Selenium 4 automated driver management (Selenium Manager).
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverFactory() {
        // Prevent direct instantiation
    }

    /**
     * Initializes a WebDriver instance based on ConfigReader defaults.
     */
    public static WebDriver initDriver() {
        return initDriver(ConfigReader.getBrowser());
    }

    /**
     * Initializes a WebDriver instance for the specified browser type.
     */
    public static WebDriver initDriver(String browser) {
        if (getDriver() != null) {
            return getDriver();
        }

        String targetBrowser = (browser == null || browser.trim().isEmpty()) 
                ? ConfigReader.getBrowser() 
                : browser.toLowerCase().trim();

        boolean headless = ConfigReader.isHeadless();
        WebDriver driver;

        switch (targetBrowser) {
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                edgeOptions.addArguments("--remote-allow-origins=*");
                edgeOptions.addArguments("--disable-gpu");
                edgeOptions.addArguments("--no-sandbox");
                edgeOptions.addArguments("--disable-dev-shm-usage");
                driver = new EdgeDriver(edgeOptions);
                break;

            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--window-size=1920,1080");
                driver = new ChromeDriver(chromeOptions);
                break;
        }

        // Apply timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));
        
        if (!headless) {
            driver.manage().window().maximize();
        }

        driverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Gets the ThreadLocal WebDriver for the current thread.
     */
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * Quits the current WebDriver session and clears the ThreadLocal reference.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
