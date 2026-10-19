package com.uplift.pages.admin;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployerAdminModel;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing an individual Employer record and its admins section.
 * Admin form fields per the application guide: name, email, mobile, economic status,
 * occupation and the PEP (Politically Exposed Person) question.
 */
public class EmployerDetailsPage extends PortalPage {

    private final By adminsSection = By.xpath("//*[self::a or self::button or @role='tab']"
            + "[contains(translate(normalize-space(.),'ADMINS','admins'),'admins')]");
    private final By addAdminButton = actionWithText("add", "admin");

    // Add admin form
    private final By firstNameInput = fieldByLabel("first name", "forename");
    private final By lastNameInput = fieldByLabel("last name", "surname");
    private final By fullNameInput = fieldByLabel("full name", "name");
    private final By emailInput = fieldByLabel("email");
    private final By mobileInput = fieldByLabel("mobile", "phone");
    private final By economicStatusInput = fieldByLabel("economic status");
    private final By occupationInput = fieldByLabel("occupation");
    private final By saveAdminButton = By.xpath("//*[@role='dialog' or self::form]//button[@type='submit']"
            + " | //button[contains(translate(normalize-space(.),'SAVEINVTD','saveinvtd'),'save')"
            + " or contains(translate(normalize-space(.),'SAVEINVTD','saveinvtd'),'invite')"
            + " or translate(normalize-space(.),'ADD','add')='add']");

    public EmployerDetailsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Opens the admins section (a tab or link); no-op when it is already shown on the page.
     */
    public void openAdminsSection() {
        if (isDisplayed(adminsSection, ConfigReader.getShortWait())) {
            click(adminsSection);
        }
    }

    public void clickAddAdmin() {
        click(addAdminButton);
    }

    /**
     * Fills the admin form. Supports both "first name + last name" and a single "name" field.
     */
    public void fillAdminForm(EmployerAdminModel admin) {
        if (isDisplayed(firstNameInput, ConfigReader.getShortWait())) {
            setField(firstNameInput, admin.getFirstName());
            setField(lastNameInput, admin.getLastName());
        } else {
            setField(fullNameInput, admin.getFullName());
        }
        setField(emailInput, admin.getEmail());
        setField(mobileInput, admin.getMobileNumber());
        setDropdown(economicStatusInput, admin.getEconomicStatus(), true);
        setDropdown(occupationInput, admin.getOccupation(), true);
        answerPepQuestion(admin.getPepAnswer());
    }

    private void answerPepQuestion(String answer) {
        try {
            answerQuestion("politically exposed", answer);
        } catch (TimeoutException e) {
            answerQuestion("pep", answer);
        }
    }

    public void clickSaveAdmin() {
        click(saveAdminButton);
    }

    /**
     * Complete workflow to add an Employer Admin for this employer.
     */
    public void addEmployerAdmin(EmployerAdminModel admin) {
        openAdminsSection();
        clickAddAdmin();
        fillAdminForm(admin);
        clickSaveAdmin();
    }

    /**
     * Verifies that the Employer Admin appears in the employer's admin list.
     */
    public boolean isAdminListed(String email) {
        return isDisplayed(rowContaining(email), ConfigReader.getExplicitWait())
                || pageContains(email, 1);
    }
}
