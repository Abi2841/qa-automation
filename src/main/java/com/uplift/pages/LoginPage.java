package com.uplift.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Skeleton Page Object representing a standard Login Page.
 */
public class LoginPage extends BasePage {

    // Locators
    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By flashMessage = By.id("flash");

    public LoginPage(WebDriver driver) {
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

    public boolean isFlashMessageDisplayed() {
        return isDisplayed(flashMessage);
    }

    public String getFlashMessageText() {
        return getText(flashMessage);
    }
}
