package com.uplift.pages.employer;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Employer Portal Login Page.
 */
public class EmployerLoginPage extends BasePage {

    // Selectors
    private final By emailInput = By.cssSelector("input#username, input#email, input[name='email'], input[type='email']");
    private final By passwordInput = By.cssSelector("input#password, input[name='password'], input[type='password']");
    private final By loginButton = By.cssSelector("button[type='submit'], button#login-btn, button.btn-primary");
    private final By errorMessageBanner = By.cssSelector(".alert-danger, .error-message, [role='alert'], #flash");

    public EmployerLoginPage(WebDriver driver) {
        super(driver);
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
     * Fluent action: logs into Employer Portal and returns EmployerDashboardPage.
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

    public boolean isLoginPageDisplayed() {
        return isDisplayed(loginButton);
    }
}
