package com.uplift.utils;

import com.uplift.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Reusable explicit wait utilities built on Selenium WebDriverWait / FluentWait.
 * No Thread.sleep: every wait polls for a condition and stops as soon as it is met.
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

    /**
     * Waits for the first matching element that is actually displayed. Unlike visibilityOfElementLocated,
     * this skips hidden matches (e.g. a collapsed mobile menu that appears earlier in the DOM).
     */
    public static WebElement waitForFirstVisible(WebDriver driver, By locator, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds)
                .withMessage("no visible element matching " + locator)
                .until(d -> d.findElements(locator).stream()
                .filter(WaitUtils::isDisplayedSafely)
                .findFirst()
                .orElse(null));
    }

    /**
     * Waits for the first matching element that is displayed and enabled.
     */
    public static WebElement waitForFirstClickable(WebDriver driver, By locator, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds)
                .withMessage("no clickable element matching " + locator)
                .until(d -> d.findElements(locator).stream()
                .filter(e -> isDisplayedSafely(e) && e.isEnabled())
                .findFirst()
                .orElse(null));
    }

    /**
     * Waits until an arbitrary browser condition returns a non-null / true value.
     */
    public static <T> T waitUntil(WebDriver driver, Function<WebDriver, T> condition, int timeoutSeconds) {
        return createWait(driver, timeoutSeconds).until(condition);
    }

    /**
     * Waits for a new, fully written file to appear in a download folder.
     *
     * @param existing files that were already present before the download was triggered
     */
    public static File waitForNewDownload(File directory, Set<String> existing, int timeoutSeconds) {
        return new FluentWait<>(directory)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .withMessage("No new file was downloaded to " + directory)
                .until(dir -> {
                    File[] files = dir.listFiles();
                    if (files == null) {
                        return null;
                    }
                    Optional<File> newest = Arrays.stream(files)
                            .filter(f -> f.isFile() && !existing.contains(f.getName()))
                            // Browsers write partial downloads to temporary names first
                            .filter(f -> !f.getName().endsWith(".crdownload") && !f.getName().endsWith(".part")
                                    && !f.getName().endsWith(".tmp") && f.length() > 0)
                            .max(Comparator.comparingLong(File::lastModified));
                    return newest.orElse(null);
                });
    }

    public static boolean isDisplayedSafely(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            // Stale or detached elements count as not displayed
            return false;
        }
    }
}
