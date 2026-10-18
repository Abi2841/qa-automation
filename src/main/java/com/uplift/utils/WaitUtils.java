package com.uplift.utils;

import com.uplift.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Reusable explicit wait utilities built on Selenium WebDriverWait.
 */
public final class WaitUtils {

    private WaitUtils() {
        // Prevent direct instantiation
    }

    private static WebDriverWait createWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public static WebElement waitForVisibility(WebDriver driver, By locator) {
        return waitForVisibility(driver, locator, ConfigReader.getExplicitWait());
    }

    public static WebElement waitForVisibility(WebDriver driver, By locator, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, ConfigReader.getExplicitWait());
    }

    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForPresence(WebDriver driver, By locator) {
        return waitForPresence(driver, locator, ConfigReader.getExplicitWait());
    }

    public static WebElement waitForPresence(WebDriver driver, By locator, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static boolean waitForUrlContains(WebDriver driver, String partialUrl) {
        return waitForUrlContains(driver, partialUrl, ConfigReader.getExplicitWait());
    }

    public static boolean waitForUrlContains(WebDriver driver, String partialUrl, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds).until(ExpectedConditions.urlContains(partialUrl));
    }
}
