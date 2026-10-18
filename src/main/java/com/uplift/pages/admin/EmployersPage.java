package com.uplift.pages.admin;

import com.uplift.pages.BasePage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object representing the Employers Listing table and search view.
 */
public class EmployersPage extends BasePage {

    // Selectors
    private final By createEmployerButton = By.cssSelector("button#add-employer, a[href*='create'], [data-testid='btn-create-employer'], .btn-add-employer");
    private final By searchInput = By.cssSelector("input[type='search'], input[placeholder*='Search'], input#employer-search");
    private final By employerTableRows = By.cssSelector("table tbody tr");
    private final By successToast = By.cssSelector(".toast-success, .alert-success, [role='status'], .notification-success");
    private final By emptyTableState = By.cssSelector(".no-data, .empty-state, td[colspan]");

    public EmployersPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the 'Add / Create Employer' button to open the creation form.
     */
    public CreateEmployerPage clickCreateEmployer() {
        click(createEmployerButton);
        return new CreateEmployerPage(driver);
    }

    public void searchEmployer(String companyName) {
        if (isDisplayed(searchInput)) {
            sendKeys(searchInput, companyName);
        }
    }

    /**
     * Checks if an employer with the given company name is displayed in the table.
     */
    public boolean isEmployerListed(String companyName) {
        WaitUtils.waitForPresence(driver, employerTableRows, 15);
        List<WebElement> rows = driver.findElements(employerTableRows);
        for (WebElement row : rows) {
            if (row.getText().contains(companyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Opens an employer record from the listing to view details and manage admins.
     */
    public EmployerDetailsPage openEmployer(String companyName) {
        By employerLink = By.xpath("//table//tr[contains(., '" + companyName + "')]//a | //table//a[contains(text(), '" + companyName + "')]");
        click(employerLink);
        return new EmployerDetailsPage(driver);
    }

    public String getSuccessToastText() {
        return getText(successToast);
    }

    public boolean isSuccessToastDisplayed() {
        return isDisplayed(successToast);
    }
}
