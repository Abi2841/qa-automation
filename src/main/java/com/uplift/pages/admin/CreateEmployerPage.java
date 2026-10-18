package com.uplift.pages.admin;

import com.uplift.models.EmployerModel;
import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Creation form.
 * Handles input of Company Details, CRN, Address, Payroll Date, and Bank Details.
 */
public class CreateEmployerPage extends BasePage {

    // 1. Company Information Locators
    private final By companyNameInput = By.cssSelector("input#companyName, input[name='companyName'], input[name='name']");
    private final By crnInput = By.cssSelector("input#crn, input[name='crn'], input[name='registrationNumber']");

    // 2. Address Locators
    private final By addressLine1Input = By.cssSelector("input#addressLine1, input[name='addressLine1'], input[name='address']");
    private final By cityInput = By.cssSelector("input#city, input[name='city']");
    private final By postalCodeInput = By.cssSelector("input#postalCode, input[name='postalCode'], input[name='zipCode']");
    private final By countryInput = By.cssSelector("input#country, select#country, input[name='country']");

    // 3. Payroll Schedule Locators
    private final By payrollDateInput = By.cssSelector("input#payrollDate, select#payrollDate, input[name='payrollDate']");

    // 4. Banking Information Locators
    private final By bankNameInput = By.cssSelector("input#bankName, input[name='bankName']");
    private final By accountNumberInput = By.cssSelector("input#accountNumber, input[name='accountNumber']");
    private final By sortCodeInput = By.cssSelector("input#sortCode, input[name='sortCode']");

    // Actions
    private final By saveButton = By.cssSelector("button[type='submit'], button#save-employer, [data-testid='btn-save'], .btn-primary");
    private final By cancelButton = By.cssSelector("button#cancel, .btn-secondary");

    public CreateEmployerPage(WebDriver driver) {
        super(driver);
    }

    public void enterCompanyName(String companyName) {
        sendKeys(companyNameInput, companyName);
    }

    public void enterCRN(String crn) {
        sendKeys(crnInput, crn);
    }

    public void enterAddress(String address, String city, String postalCode) {
        sendKeys(addressLine1Input, address);
        sendKeys(cityInput, city);
        sendKeys(postalCodeInput, postalCode);
    }

    public void enterPayrollDate(String payrollDate) {
        sendKeys(payrollDateInput, payrollDate);
    }

    public void enterBankDetails(String bankName, String accountNumber, String sortCode) {
        sendKeys(bankNameInput, bankName);
        sendKeys(accountNumberInput, accountNumber);
        sendKeys(sortCodeInput, sortCode);
    }

    public void clickSave() {
        click(saveButton);
    }

    /**
     * Complete workflow to populate all employer sections and submit.
     */
    public EmployersPage createEmployer(EmployerModel employer) {
        enterCompanyName(employer.getCompanyName());
        enterCRN(employer.getCompanyRegistrationNumber());
        enterAddress(employer.getAddressLine1(), employer.getCity(), employer.getPostalCode());
        enterPayrollDate(employer.getPayrollDate());
        enterBankDetails(employer.getBankName(), employer.getAccountNumber(), employer.getSortCode());
        clickSave();
        return new EmployersPage(driver);
    }
}
