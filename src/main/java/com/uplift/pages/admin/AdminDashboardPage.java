package com.uplift.pages.admin;

import com.uplift.config.ConfigReader;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Admin Portal Dashboard.
 * Handles top-level navigation.
 */
public class AdminDashboardPage extends PortalPage {

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * The dashboard is considered loaded once the "Employers" menu item is visible.
     */
    public boolean isDashboardLoaded() {
        return isDisplayed(navItemExactly("Employers"), ConfigReader.getExplicitWait())
                || isDisplayed(actionWithText("Employers"), 1);
    }

    /**
     * Navigates to the Employers management section.
     */
    public EmployersPage navigateToEmployers() {
        navigateTo("Employers");
        return new EmployersPage(driver);
    }
}
