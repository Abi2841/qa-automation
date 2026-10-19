package com.uplift.pages.admin;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployerModel;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the "Add employer" form.
 * Sections per the application guide: company details (name, CRN, address with lookup),
 * payroll date and bank details (IBAN, BIC).
 */
public class CreateEmployerPage extends PortalPage {

    // Company details
    private final By companyNameInput = fieldByLabel("company name", "employer name", "legal name");
    private final By crnInput = fieldByLabel("crn", "registration number", "company number");

    // Address lookup: type a query, then pick the first suggestion
    private final By addressLookupInput = fieldByLabel("address lookup", "search address", "find address", "address", "eircode");
    private final By addressSuggestion = By.cssSelector(
            "[role='option'], [role='listbox'] li, .pac-item, [class*='suggestion'] li, [class*='suggestion-item'], [class*='autocomplete'] li");

    // Payroll and bank details
    private final By payrollDateInput = fieldByLabel("payroll date", "payroll day", "payroll");
    private final By ibanInput = fieldByLabel("iban");
    private final By bicInput = fieldByLabel("bic", "swift");

    private final By saveButton = By.xpath("//form//button[@type='submit'] | " + actionWithTextXpath());

    public CreateEmployerPage(WebDriver driver) {
        super(driver);
    }

    private static String actionWithTextXpath() {
        return "//button[contains(translate(normalize-space(.),'SAVECRT','savecrt'),'save')"
                + " or contains(translate(normalize-space(.),'SAVECRT','savecrt'),'create')]";
    }

    public void enterCompanyName(String companyName) {
        setField(companyNameInput, companyName);
    }

    public void enterCRN(String crn) {
        setField(crnInput, crn);
    }

    /**
     * Uses the address lookup: types the query and selects the first suggestion offered.
     */
    public void selectAddress(String lookupQuery) {
        setField(addressLookupInput, lookupQuery);
        if (!isDisplayed(addressSuggestion, ConfigReader.getExplicitWait())) {
            throw new IllegalStateException("Address lookup offered no suggestions for '" + lookupQuery + "'");
        }
        click(addressSuggestion);
    }

    public void enterPayrollDate(String payrollDay) {
        setDropdown(payrollDateInput, payrollDay, false);
    }

    public void enterBankDetails(String iban, String bic) {
        setField(ibanInput, iban);
        setField(bicInput, bic);
    }

    public void clickSave() {
        click(saveButton);
    }

    /**
     * Complete workflow to populate all employer sections and save.
     */
    public void createEmployer(EmployerModel employer) {
        enterCompanyName(employer.getCompanyName());
        enterCRN(employer.getCompanyRegistrationNumber());
        selectAddress(employer.getAddressLookupQuery());
        enterPayrollDate(employer.getPayrollDay());
        enterBankDetails(employer.getIban(), employer.getBic());
        clickSave();
    }
}
