package com.uplift.pages.employer;

import com.uplift.config.ConfigReader;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Employee Directory.
 * Entry point for "Add employee" and "Bulk upload".
 */
public class EmployeesPage extends PortalPage {

    private final By addEmployeeButton = actionWithText("add", "employee");
    private final By bulkUploadButton = actionWithText("bulk");
    private final By searchInput = By.cssSelector("input[type='search'], input[placeholder*='earch'], input[aria-label*='earch']");

    public EmployeesPage(WebDriver driver) {
        super(driver);
    }

    public AddEmployeePage clickAddEmployee() {
        click(addEmployeeButton);
        return new AddEmployeePage(driver);
    }

    public BulkUploadPage clickBulkUpload() {
        click(bulkUploadButton);
        return new BulkUploadPage(driver);
    }

    public void searchEmployee(String searchTerm) {
        if (isDisplayed(searchInput, ConfigReader.getShortWait())) {
            sendKeys(searchInput, searchTerm);
        }
    }

    /**
     * Verifies that an employee matching the identifier (e.g. email) appears in the list.
     */
    public boolean isEmployeeListed(String identifier) {
        return isEmployeeListed(identifier, ConfigReader.getExplicitWait());
    }

    /**
     * Use a short timeout when asserting that an employee is NOT listed.
     */
    public boolean isEmployeeListed(String identifier, int timeoutSeconds) {
        searchEmployee(identifier);
        return isDisplayed(rowContaining(identifier), timeoutSeconds);
    }

    /**
     * Returns the employer dashboard view (menu is shared by every portal page).
     */
    public EmployerDashboardPage dashboard() {
        return new EmployerDashboardPage(driver);
    }
}
