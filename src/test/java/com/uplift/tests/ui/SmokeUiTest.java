package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UI smoke test verifying Selenium WebDriver initialization,
 * navigation, and Page Object Model interaction.
 */
public class SmokeUiTest extends BaseTest {

    @Test(description = "Verify WebDriver initializes and successfully navigates to base QA URL")
    public void testBrowserLaunchAndNavigation() {
        navigateToBaseUrl();
        String currentUrl = driver.getCurrentUrl();
        String title = driver.getTitle();

        Assert.assertNotNull(currentUrl, "Current URL should not be null");
        Assert.assertTrue(currentUrl.contains("the-internet.herokuapp.com"), "URL should match target application");
        Assert.assertNotNull(title, "Page title should not be null");
    }

    @Test(description = "Verify LoginPage object can interact with form elements")
    public void testLoginPageInteraction() {
        driver.get(ConfigReader.getBaseUrl() + "/login");
        LoginPage loginPage = new LoginPage(driver);

        loginPage.enterUsername("tomsmith");
        loginPage.enterPassword("SuperSecretPassword!");
        loginPage.clickLogin();

        Assert.assertTrue(loginPage.isFlashMessageDisplayed(), "Flash notification should appear on submit");
    }
}
