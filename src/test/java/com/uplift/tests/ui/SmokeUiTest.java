package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Framework self-test (env=demo): verifies Selenium WebDriver initialization,
 * navigation, and Page Object Model interaction against a public sandbox.
 */
public class SmokeUiTest extends BaseTest {

    @Test(description = "Verify WebDriver initializes and successfully navigates to the configured base URL")
    public void testBrowserLaunchAndNavigation() {
        driver.get(ConfigReader.getBaseUrl());

        Assert.assertTrue(driver.getCurrentUrl().startsWith(ConfigReader.getBaseUrl()),
                "Browser should be on the configured base URL but was " + driver.getCurrentUrl());
        Assert.assertFalse(driver.getTitle().isEmpty(), "Page title should not be empty");
    }

    @Test(description = "Verify the LoginPage object drives the form and reads the error for unknown credentials")
    public void testLoginPageRejectsUnknownUser() {
        driver.get(ConfigReader.getBaseUrl() + "/login");
        LoginPage loginPage = new LoginPage(driver);

        loginPage.enterUsername("QA-AUTO-unknown-user");
        loginPage.enterPassword("not-a-real-password");
        loginPage.clickLogin();

        Assert.assertTrue(loginPage.isFlashMessageDisplayed(), "Flash notification should appear on submit");
        Assert.assertTrue(loginPage.getFlashMessageText().toLowerCase().contains("invalid"),
                "Flash message should report invalid credentials: " + loginPage.getFlashMessageText());
    }
}
