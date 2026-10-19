package com.uplift.pages.employer;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Portal Login Page.
 */
public class EmployerLoginPage extends BasePage {

    private final By emailInput = By.cssSelector(
            "input[type='email'], input[name='email'], input[name='username'], input#username, input#email");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By loginButton = By.xpath("//button[@type='submit'] | //input[@type='submit']");
    private final By errorMessageBanner = By.cssSelector("[role='alert'], .alert-danger, .error-message, [class*='error']");

    public EmployerLoginPage(WebDriver driver) {
        super(driver);
    }

    public EmployerLoginPage open(String url) {
        driver.get(url);
        return this;
    }

    public void enterEmail(String email) {
        sendKeys(emailInput, email);
    }

    public void enterPassword(String password) {
        sendKeys(passwordInput, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    /**
     * Reusable login helper: logs into the Employer Portal and returns the dashboard.
     */
    public EmployerDashboardPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        return new EmployerDashboardPage(driver);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessageBanner);
    }

    public String getErrorMessageText() {
        return getText(errorMessageBanner);
    }

    /**
     * True when the login form is shown (password field and submit button visible).
     */
    public boolean isLoginPageDisplayed() {
        return isDisplayed(passwordInput) && isDisplayed(loginButton, 1);
    }
}
