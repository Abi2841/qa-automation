package com.uplift.base;

import com.uplift.config.ConfigReader;
import com.uplift.reporting.ExtentTestListener;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base test class for UI automation test cases.
 * Handles WebDriver lifecycle per test method.
 */
@Listeners(ExtentTestListener.class)
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    /**
     * Helper to navigate directly to the configured QA base URL.
     */
    protected void navigateToBaseUrl() {
        if (driver != null) {
            driver.get(ConfigReader.getBaseUrl());
        }
    }

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
