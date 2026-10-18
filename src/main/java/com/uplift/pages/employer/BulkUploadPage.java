package com.uplift.pages.employer;

import com.uplift.pages.BasePage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object representing the Bulk Employee CSV Upload Page.
 * Handles template testt download, file upload, and partial success/failure
 * verification.
 */
public class BulkUploadPage extends BasePage {

    // Upload Controls
    private final By fileInput = By.cssSelector("input[type='file'], input#csv-upload, input[name='file']");
    private final By uploadSubmitButton = By.cssSelector(
            "button#submit-upload, button.btn-upload, [data-testid='btn-submit-upload'], button[type='submit']");
    private final By downloadTemplateLink = By
            .cssSelector("a[href*='template'], [data-testid='download-template'], .link-template");

    // Processing Result Badges & Tables
    private final By uploadResultContainer = By
            .cssSelector(".upload-result, .upload-summary, [data-testid='upload-summary']");
    private final By successRecordsCountBadge = By
            .cssSelector(".badge-success, .valid-records-count, [data-testid='count-success']");
    private final By failedRecordsCountBadge = By
            .cssSelector(".badge-danger, .invalid-records-count, [data-testid='count-failed']");
    private final By rejectedErrorRows = By
            .cssSelector("table.rejected-records tbody tr, .error-item, table.upload-errors tbody tr");
    private final By backToEmployeesButton = By.cssSelector("a[href*='employee'], [data-testid='btn-back'], .btn-back");

    public BulkUploadPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the portal's native template link to download the actual CSV format.
     */
    public void clickDownloadTemplate() {
        click(downloadTemplateLink);
    }

    /**
     * Uploads the specified CSV file by feeding the absolute path directly into the
     * input[type='file'].
     * Avoids flaky OS native file dialogs.
     */
    public void uploadFile(String absoluteFilePath) {
        File file = new File(absoluteFilePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Upload file does not exist at path: " + absoluteFilePath);
        }

        // Selenium standard: send absolute file path to the file input
        WaitUtils.waitForPresence(driver, fileInput, 15);
        driver.findElement(fileInput).sendKeys(file.getAbsolutePath());

        click(uploadSubmitButton);
    }

    /**
     * Waits for upload batch processing to complete.
     */
    public boolean isUploadProcessed() {
        return isDisplayed(uploadResultContainer) || isDisplayed(successRecordsCountBadge);
    }

    /**
     * Parses the count of successfully imported records (expects 3).
     */
    public int getSuccessRecordsCount() {
        try {
            String text = getText(successRecordsCountBadge).replaceAll("[^0-9]", "");
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parses the count of rejected records (expects 2).
     */
    public int getFailedRecordsCount() {
        try {
            String text = getText(failedRecordsCountBadge).replaceAll("[^0-9]", "");
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retrieves all error descriptions for rejected rows.
     */
    public List<String> getRejectedRowErrors() {
        List<String> errors = new ArrayList<>();
        List<WebElement> rows = driver.findElements(rejectedErrorRows);
        for (WebElement row : rows) {
            errors.add(row.getText());
        }
        return errors;
    }

    /**
     * Verifies that error text contains the expected reason (e.g. invalid email or
     * duplicate ID).
     */
    public boolean containsErrorMessage(String expectedSubstring) {
        List<String> errors = getRejectedRowErrors();
        for (String err : errors) {
            if (err.toLowerCase().contains(expectedSubstring.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public EmployeesPage clickBackToEmployees() {
        click(backToEmployeesButton);
        return new EmployeesPage(driver);
    }
}
