package com.uplift.pages;

import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Base Page Object encapsulating common browser actions and explicit wait strategies.
 */
public abstract class BasePage {

    protected final WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected void click(By locator) {
        WaitUtils.waitForClickable(driver, locator).click();
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = WaitUtils.waitForVisibility(driver, locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return WaitUtils.waitForVisibility(driver, locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return WaitUtils.waitForVisibility(driver, locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
