package com.uplift.pages.employer;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployeeModel;
import com.uplift.pages.PortalPage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Page Object representing the "Add employee" form.
 * Fields per the application guide: name, email, mobile, date of birth, PPSN and start date.
 */
public class AddEmployeePage extends PortalPage {

    private final By firstNameInput = fieldByLabel("first name", "forename");
    private final By lastNameInput = fieldByLabel("last name", "surname");
    private final By emailInput = fieldByLabel("email");
    private final By mobileInput = fieldByLabel("mobile", "phone");
    private final By dobInput = fieldByLabel("date of birth", "birth", "dob");
    private final By ppsnInput = fieldByLabel("ppsn", "pps number", "pps no");
    private final By startDateInput = fieldByLabel("start date", "employment start", "date of joining");

    private final By saveButton = By.xpath("//*[@role='dialog' or self::form]//button[@type='submit']"
            + " | //button[contains(translate(normalize-space(.),'SAVEDMPLOY','savedmploy'),'save')"
            + " or translate(normalize-space(.),'ADEMPLOY','ademploy')='add employee'"
            + " or translate(normalize-space(.),'AD','ad')='add']");
    private final By invalidHtml5Fields = By.cssSelector("input:invalid, select:invalid, textarea:invalid");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Fills every field that has a value; null/empty values leave the field blank.
     */
    public void fillEmployeeForm(EmployeeModel employee) {
        visible(firstNameInput);
        setField(firstNameInput, employee.getFirstName());
        setField(lastNameInput, employee.getLastName());
        setField(emailInput, employee.getEmail());
        setField(mobileInput, employee.getMobileNumber());
        setDate(dobInput, employee.getDateOfBirth());
        setField(ppsnInput, employee.getPpsn());
        setDate(startDateInput, employee.getStartDate());
    }

    public void clickSave() {
        click(saveButton);
    }

    /**
     * Complete workflow to add a single valid employee and submit.
     */
    public void addEmployee(EmployeeModel employee) {
        fillEmployeeForm(employee);
        clickSave();
    }

    /**
     * Waits for validation feedback after a submit and returns all messages found:
     * visible error elements (inline or banner) and the browser's HTML5 validation messages.
     * Returns an empty list if no feedback appears within the timeout.
     */
    public List<String> getValidationMessages(int timeoutSeconds) {
        try {
            return WaitUtils.waitUntil(driver, d -> {
                List<String> messages = new ArrayList<>();
                for (WebElement element : d.findElements(VALIDATION_MESSAGE)) {
                    if (WaitUtils.isDisplayedSafely(element) && !element.getText().trim().isEmpty()) {
                        messages.add(element.getText().trim());
                    }
                }
                for (WebElement field : d.findElements(invalidHtml5Fields)) {
                    String message = field.getDomProperty("validationMessage");
                    if (message != null && !message.isEmpty()) {
                        messages.add("[HTML5] " + message);
                    }
                }
                return messages.isEmpty() ? null : messages;
            }, timeoutSeconds);
        } catch (TimeoutException e) {
            return Collections.emptyList();
        }
    }

    /**
     * True while the form is still displayed (i.e. the submit was blocked).
     */
    public boolean isFormStillDisplayed() {
        return isDisplayed(firstNameInput, ConfigReader.getShortWait());
    }
}
