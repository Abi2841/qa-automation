package com.uplift.base;

import com.uplift.reporting.ExtentTestListener;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Base test class for UI automation test cases.
 * One browser per test class: journey steps are separate @Test methods that share the
 * logged-in session, and the browser stays open until @AfterClass so the listener can
 * always capture a failure screenshot.
 */
@Listeners(ExtentTestListener.class)
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.initDriver();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
