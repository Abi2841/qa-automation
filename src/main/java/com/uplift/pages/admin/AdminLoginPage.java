package com.uplift.pages.admin;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Super Admin Portal Login Page.
 * Locators are centralized here so when the actual selectors are inspected,
 * they only need to be adjusted in this single file.
 */
public class AdminLoginPage extends BasePage {

    // Selectors (Placeholder defaults that can be swapped with real QA attributes)
    private final By usernameInput = By.cssSelector("input#username, input[name='username'], input[type='email'], input[name='email']");
    private final By passwordInput = By.cssSelector("input#password, input[name='password'], input[type='password']");
    private final By loginButton = By.cssSelector("button[type='submit'], button#login-btn, button.btn-primary");
    private final By errorMessageBanner = By.cssSelector(".alert-danger, .error-message, [role='alert'], #flash");
    private final By rememberMeCheckbox = By.cssSelector("input[type='checkbox']#rememberMe, input[name='rememberMe']");

    public AdminLoginPage(WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        sendKeys(usernameInput, username);
    }

    public void enterPassword(String password) {
        sendKeys(passwordInput, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    /**
     * Fluent action: logs in with specified credentials and navigates towards AdminDashboardPage.
     */
    public AdminDashboardPage loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        return new AdminDashboardPage(driver);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessageBanner);
    }

    public String getErrorMessage() {
        return getText(errorMessageBanner);
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(loginButton);
    }
}
