package com.uplift.pages.admin;

import com.uplift.models.EmployerAdminModel;
import com.uplift.pages.BasePage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object representing an individual Employer profile page.
 * Manages Employer details and provisioning of Employer Admins.
 */
public class EmployerDetailsPage extends BasePage {

    // Top Level Locators
    private final By employerHeading = By.cssSelector("h1, h2.employer-name, [data-testid='employer-title']");
    private final By addAdminButton = By.cssSelector("button#add-admin, [data-testid='btn-add-admin'], .btn-add-admin");
    private final By adminsTableRows = By.cssSelector("table#admins-table tbody tr, .admin-list-row");
    private final By adminSuccessNotification = By.cssSelector(".toast-success, .alert-success, [role='status']");

    // Add Admin Form / Modal Locators
    private final By adminFirstNameInput = By.cssSelector("input#firstName, input[name='firstName']");
    private final By adminLastNameInput = By.cssSelector("input#lastName, input[name='lastName']");
    private final By adminEmailInput = By.cssSelector("input#email, input[name='email'], input[type='email']");
    private final By adminPhoneInput = By.cssSelector("input#phone, input[name='phone'], input[name='phoneNumber']");
    private final By saveAdminButton = By.cssSelector("button#save-admin, [data-testid='btn-save-admin'], button.btn-save");

    public EmployerDetailsPage(WebDriver driver) {
        super(driver);
    }

    public String getEmployerHeading() {
        return getText(employerHeading);
    }

    public void clickAddAdmin() {
        click(addAdminButton);
    }

    public void enterAdminFirstName(String firstName) {
        sendKeys(adminFirstNameInput, firstName);
    }

    public void enterAdminLastName(String lastName) {
        sendKeys(adminLastNameInput, lastName);
    }

    public void enterAdminEmail(String email) {
        sendKeys(adminEmailInput, email);
    }

    public void enterAdminPhone(String phone) {
        sendKeys(adminPhoneInput, phone);
    }

    public void clickSaveAdmin() {
        click(saveAdminButton);
    }

    /**
     * Complete workflow to provision an Employer Admin for this company.
     */
    public EmployerDetailsPage addEmployerAdmin(EmployerAdminModel admin) {
        clickAddAdmin();
        enterAdminFirstName(admin.getFirstName());
        enterAdminLastName(admin.getLastName());
        enterAdminEmail(admin.getEmail());
        enterAdminPhone(admin.getPhoneNumber());
        clickSaveAdmin();
        return this;
    }

    /**
     * Verifies that the created Employer Admin appears in the employer's admin roster.
     */
    public boolean isAdminListed(String emailOrName) {
        WaitUtils.waitForPresence(driver, adminsTableRows, 15);
        List<WebElement> rows = driver.findElements(adminsTableRows);
        for (WebElement row : rows) {
            if (row.getText().contains(emailOrName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuccessNotificationDisplayed() {
        return isDisplayed(adminSuccessNotification);
    }
}
