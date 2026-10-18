package com.uplift.pages.employer;

import com.uplift.models.EmployeeModel;
import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Object representing the Manual Employee Onboarding form.
 * Supports standard fields plus Irish regulatory fields: PPSN, Mobile, Date of Birth,
 * and comprehensive validation error inspection.
 */
public class AddEmployeePage extends BasePage {

    // Core Form Locators
    private final By employeeIdInput = By.cssSelector("input#employeeId, input[name='employeeId'], input[name='staffId']");
    private final By firstNameInput = By.cssSelector("input#firstName, input[name='firstName']");
    private final By lastNameInput = By.cssSelector("input#lastName, input[name='lastName']");
    private final By emailInput = By.cssSelector("input#email, input[name='email'], input[type='email']");
    private final By departmentInput = By.cssSelector("input#department, select#department, input[name='department']");
    private final By dateOfJoiningInput = By.cssSelector("input#dateOfJoining, input[name='dateOfJoining'], input[type='date']");
    private final By salaryInput = By.cssSelector("input#salary, input[name='salary']");

    // Irish Regulatory & Contact Locators
    private final By ppsnInput = By.cssSelector("input#ppsn, input[name='ppsn'], input[data-testid='ppsn-input']");
    private final By mobileInput = By.cssSelector("input#mobile, input#phone, input[name='mobile'], input[name='mobileNumber']");
    private final By dobInput = By.cssSelector("input#dob, input[name='dob'], input[name='dateOfBirth'], input[type='date']#dob");

    // Action Buttons
    private final By saveButton = By.cssSelector("button[type='submit'], button#save-employee, [data-testid='btn-save-employee'], .btn-primary");
    private final By cancelButton = By.cssSelector("button#cancel, .btn-cancel, .btn-secondary");

    // Validation Feedback & Error Locators
    private final By generalErrorBanner = By.cssSelector(".alert-danger, .error-summary, [role='alert'], .toast-error, .banner-error");
    private final By inlineFieldErrors = By.cssSelector(".invalid-feedback, .error-message, .field-error, span[id*='error'], .text-danger");
    private final By emailError = By.cssSelector("#email-error, [data-testid='error-email'], .email-error");
    private final By ppsnError = By.cssSelector("#ppsn-error, [data-testid='error-ppsn'], .ppsn-error");
    private final By mobileError = By.cssSelector("#mobile-error, [data-testid='error-mobile'], .mobile-error");
    private final By dobError = By.cssSelector("#dob-error, [data-testid='error-dob'], .dob-error");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public void enterEmployeeId(String employeeId) {
        sendKeys(employeeIdInput, employeeId);
    }

    public void enterFirstName(String firstName) {
        sendKeys(firstNameInput, firstName);
    }

    public void enterLastName(String lastName) {
        sendKeys(lastNameInput, lastName);
    }

    public void enterEmail(String email) {
        sendKeys(emailInput, email);
    }

    public void enterDepartment(String department) {
        sendKeys(departmentInput, department);
    }

    public void enterDateOfJoining(String doj) {
        sendKeys(dateOfJoiningInput, doj);
    }

    public void enterSalary(String salary) {
        sendKeys(salaryInput, salary);
    }

    public void enterPpsn(String ppsn) {
        sendKeys(ppsnInput, ppsn);
    }

    public void enterMobile(String mobile) {
        sendKeys(mobileInput, mobile);
    }

    public void enterDateOfBirth(String dob) {
        sendKeys(dobInput, dob);
    }

    public void clickSave() {
        click(saveButton);
    }

    /**
     * Submits the form expecting validation failure.
     * Keeps user on the same page for error inspection.
     */
    public AddEmployeePage submitExpectingValidationFailure() {
        click(saveButton);
        return this;
    }

    /**
     * Complete workflow to populate all employee fields (including PPSN, Mobile, DOB).
     */
    public void fillEmployeeForm(EmployeeModel employee) {
        if (employee.getEmployeeId() != null) enterEmployeeId(employee.getEmployeeId());
        if (employee.getFirstName() != null) enterFirstName(employee.getFirstName());
        if (employee.getLastName() != null) enterLastName(employee.getLastName());
        if (employee.getEmail() != null) enterEmail(employee.getEmail());
        if (employee.getPpsn() != null) enterPpsn(employee.getPpsn());
        if (employee.getMobileNumber() != null) enterMobile(employee.getMobileNumber());
        if (employee.getDateOfBirth() != null) enterDateOfBirth(employee.getDateOfBirth());
        if (employee.getDepartment() != null) enterDepartment(employee.getDepartment());
        if (employee.getDateOfJoining() != null) enterDateOfJoining(employee.getDateOfJoining());
        if (employee.getSalary() != null) enterSalary(employee.getSalary());
    }

    /**
     * Complete workflow to manually add a single valid employee and submit.
     */
    public EmployeesPage addEmployee(EmployeeModel employee) {
        fillEmployeeForm(employee);
        clickSave();
        return new EmployeesPage(driver);
    }

    // Validation Inspection Helpers
    public boolean isGeneralErrorDisplayed() {
        return isDisplayed(generalErrorBanner);
    }

    public String getGeneralErrorMessage() {
        return getText(generalErrorBanner);
    }

    public boolean isEmailErrorDisplayed() {
        return isDisplayed(emailError);
    }

    public String getEmailErrorMessage() {
        return getText(emailError);
    }

    public boolean isPpsnErrorDisplayed() {
        return isDisplayed(ppsnError);
    }

    public String getPpsnErrorMessage() {
        return getText(ppsnError);
    }

    public boolean isMobileErrorDisplayed() {
        return isDisplayed(mobileError);
    }

    public String getMobileErrorMessage() {
        return getText(mobileError);
    }

    public boolean isDobErrorDisplayed() {
        return isDisplayed(dobError);
    }

    public String getDobErrorMessage() {
        return getText(dobError);
    }

    /**
     * Returns all visible inline error texts on the form.
     */
    public List<String> getAllInlineErrorMessages() {
        List<String> errors = new ArrayList<>();
        List<WebElement> elements = driver.findElements(inlineFieldErrors);
        for (WebElement el : elements) {
            if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                errors.add(el.getText().trim());
            }
        }
        return errors;
    }

    /**
     * Checks if any validation failure is visible (banner, inline error, or HTML5 invalid pseudo-class).
     */
    public boolean hasValidationErrors() {
        if (isGeneralErrorDisplayed()) return true;
        List<String> inline = getAllInlineErrorMessages();
        if (!inline.isEmpty()) return true;

        // Check HTML5 :invalid state on inputs
        List<WebElement> invalidInputs = driver.findElements(By.cssSelector("input:invalid, select:invalid"));
        return !invalidInputs.isEmpty();
    }
}
