package com.uplift.pages.employer;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Portal Dashboard.
 * Allows verification that the dashboard belongs to the expected employer organization.
 */
public class EmployerDashboardPage extends BasePage {

    // Selectors
    private final By employerNameBadge = By.cssSelector(".company-name, .employer-brand, [data-testid='company-badge'], h2.org-name");
    private final By employeesNavMenu = By.cssSelector("a[href*='employee'], [data-testid='nav-employees'], nav a");
    private final By userProfileMenu = By.cssSelector(".user-menu, .profile-dropdown, [data-testid='user-profile']");
    private final By logoutButton = By.cssSelector("a[href*='logout'], [data-testid='logout-btn'], button.logout");

    public EmployerDashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Retrieves the company name shown on the employer dashboard header.
     */
    public String getEmployerCompanyName() {
        return getText(employerNameBadge);
    }

    public boolean isEmployerNameDisplayed() {
        return isDisplayed(employerNameBadge);
    }

    /**
     * Navigates to the Employees management directory.
     */
    public EmployeesPage navigateToEmployees() {
        click(employeesNavMenu);
        return new EmployeesPage(driver);
    }

    /**
     * Logs out of the Employer Portal and returns to EmployerLoginPage.
     */
    public EmployerLoginPage clickLogout() {
        if (isDisplayed(userProfileMenu)) {
            click(userProfileMenu);
        }
        click(logoutButton);
        return new EmployerLoginPage(driver);
    }
}
