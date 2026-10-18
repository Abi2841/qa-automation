package com.uplift.pages.admin;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Admin Portal Main Dashboard.
 * Handles top-level navigation, menu options, and session logout.
 */
public class AdminDashboardPage extends BasePage {

    // Selectors
    private final By dashboardHeading = By.cssSelector("h1, .dashboard-title, .page-header");
    private final By employersMenuLink = By.cssSelector("a[href*='employer'], a[data-testid='nav-employers'], nav a");
    private final By userProfileDropdown = By.cssSelector(".user-profile, .avatar, [data-testid='user-menu']");
    private final By logoutButton = By.cssSelector("a[href*='logout'], [data-testid='logout-btn'], button.logout");

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        return isDisplayed(dashboardHeading) || isDisplayed(employersMenuLink);
    }

    public String getDashboardHeading() {
        return getText(dashboardHeading);
    }

    /**
     * Navigates to the Employers management section.
     */
    public EmployersPage navigateToEmployers() {
        click(employersMenuLink);
        return new EmployersPage(driver);
    }

    /**
     * Terminates the user session.
     */
    public AdminLoginPage clickLogout() {
        if (isDisplayed(userProfileDropdown)) {
            click(userProfileDropdown);
        }
        click(logoutButton);
        return new AdminLoginPage(driver);
    }
}
