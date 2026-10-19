package com.uplift.pages.admin;

import com.uplift.config.ConfigReader;
import com.uplift.pages.PortalPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employers list (Admin portal: Employers -> Add employer).
 */
public class EmployersPage extends PortalPage {

    private final By addEmployerButton = actionWithText("add", "employer");
    private final By searchInput = By.cssSelector("input[type='search'], input[placeholder*='earch'], input[aria-label*='earch']");

    public EmployersPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks 'Add employer' to open the creation form.
     */
    public CreateEmployerPage clickAddEmployer() {
        click(addEmployerButton);
        return new CreateEmployerPage(driver);
    }

    /**
     * Filters the list when a search box exists (the list may be paginated).
     */
    public void searchEmployer(String companyName) {
        if (isDisplayed(searchInput, ConfigReader.getShortWait())) {
            sendKeys(searchInput, companyName);
        }
    }

    /**
     * Returns the full text of the employer's row (name, status, ...) or null if it is not listed.
     */
    public String getEmployerRowText(String companyName) {
        searchEmployer(companyName);
        try {
            return visible(rowContaining(companyName)).getText();
        } catch (TimeoutException e) {
            return null;
        }
    }

    /**
     * Opens an employer record from the listing to manage its admins.
     */
    public EmployerDetailsPage openEmployer(String companyName) {
        searchEmployer(companyName);
        By link = By.xpath("(//tr | //*[@role='row'] | //li)[contains(normalize-space(.),'" + companyName + "')]"
                + "//*[self::a or self::button][contains(normalize-space(.),'" + companyName + "') or "
                + "contains(translate(normalize-space(.),'VIEWOPN','viewopn'),'view') or "
                + "contains(translate(normalize-space(.),'VIEWOPN','viewopn'),'open')]"
                + " | //a[contains(normalize-space(.),'" + companyName + "')]");
        if (isDisplayed(link, ConfigReader.getShortWait())) {
            click(link);
        } else {
            // Rows that are clickable as a whole
            click(rowContaining(companyName));
        }
        return new EmployerDetailsPage(driver);
    }
}
