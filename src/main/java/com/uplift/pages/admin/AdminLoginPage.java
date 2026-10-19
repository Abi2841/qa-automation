package com.uplift.pages.admin;

import com.uplift.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object representing the Admin Portal Login Page.
 * Locators are centralized here so when the actual selectors are inspected,
 * they only need to be adjusted in this single file.
 */
public class AdminLoginPage extends BasePage {

    private final By usernameInput = By.cssSelector(
            "input[type='email'], input[name='email'], input[name='username'], input#username, input#email");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By loginButton = By.xpath("//button[@type='submit'] | //input[@type='submit']");
    private final By errorMessageBanner = By.cssSelector("[role='alert'], .alert-danger, .error-message, [class*='error']");

    public AdminLoginPage(WebDriver driver) {
        super(driver);
    }

    public AdminLoginPage open(String url) {
        driver.get(url);
        return this;
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
     * Reusable login helper: logs in and returns the dashboard.
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
        return isDisplayed(passwordInput);
    }
}
