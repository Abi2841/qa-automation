package com.uplift.pages.employer;

import com.uplift.config.ConfigReader;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Portal Dashboard.
 * Allows verification that the dashboard belongs to the expected employer organization.
 */
public class EmployerDashboardPage extends PortalPage {

    public EmployerDashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * The dashboard is considered loaded once the "Employees" menu item is visible.
     */
    public boolean isDashboardLoaded() {
        return isDisplayed(navItemExactly("Employees"), ConfigReader.getExplicitWait())
                || isDisplayed(actionWithText("Employees"), 1);
    }

    /**
     * True when the page shows the given employer's name (dashboard scoped to that employer).
     */
    public boolean isDashboardForEmployer(String companyName) {
        return pageContains(companyName, ConfigReader.getExplicitWait());
    }

    /**
     * Navigates to the Employees directory.
     */
    public EmployeesPage navigateToEmployees() {
        navigateTo("Employees");
        return new EmployeesPage(driver);
    }

    /**
     * Logs out of the Employer Portal and returns to EmployerLoginPage.
     */
    public EmployerLoginPage clickLogout() {
        logout();
        return new EmployerLoginPage(driver);
    }
}
