package com.uplift.pages.employer;

import com.uplift.pages.BasePage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object representing the Employer Employee Directory.
 * Supports manual employee creation, bulk CSV upload, and employee search.
 */
public class EmployeesPage extends BasePage {

    // Selectors
    private final By addEmployeeButton = By.cssSelector("button#add-employee, a[href*='add-employee'], [data-testid='btn-add-employee'], .btn-add");
    private final By bulkUploadButton = By.cssSelector("button#bulk-upload, a[href*='bulk'], [data-testid='btn-bulk-upload'], .btn-upload");
    private final By downloadTemplateLink = By.cssSelector("a[href*='template'], [data-testid='btn-download-template'], .link-download");
    private final By searchInput = By.cssSelector("input[type='search'], input[placeholder*='Search employees'], #employee-search");
    private final By employeeTableRows = By.cssSelector("table#employees-table tbody tr, table.employee-directory tbody tr, table tbody tr");
    private final By successToast = By.cssSelector(".toast-success, .alert-success, [role='status']");

    public EmployeesPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the 'Add Employee' button to navigate to the manual creation form.
     */
    public AddEmployeePage clickAddEmployee() {
        click(addEmployeeButton);
        return new AddEmployeePage(driver);
    }

    /**
     * Clicks the 'Bulk Upload' button to open the CSV upload view.
     */
    public BulkUploadPage clickBulkUpload() {
        click(bulkUploadButton);
        return new BulkUploadPage(driver);
    }

    /**
     * Clicks the portal link to download the actual portal CSV template.
     */
    public void clickDownloadTemplate() {
        click(downloadTemplateLink);
    }

    public void searchEmployee(String searchTerm) {
        if (isDisplayed(searchInput)) {
            sendKeys(searchInput, searchTerm);
        }
    }

    /**
     * Verifies that an employee matching the identifier (e.g., email or name or ID) appears in the table.
     */
    public boolean isEmployeeListed(String identifier) {
        WaitUtils.waitForPresence(driver, employeeTableRows, 15);
        List<WebElement> rows = driver.findElements(employeeTableRows);
        for (WebElement row : rows) {
            if (row.getText().contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    public int getEmployeeCount() {
        List<WebElement> rows = driver.findElements(employeeTableRows);
        return rows.size();
    }

    public boolean isSuccessToastDisplayed() {
        return isDisplayed(successToast);
    }

    public String getSuccessToastText() {
        return getText(successToast);
    }
}
