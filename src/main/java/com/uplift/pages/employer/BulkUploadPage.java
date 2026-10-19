package com.uplift.pages.employer;

import com.uplift.base.DriverFactory;
import com.uplift.config.ConfigReader;
import com.uplift.pages.PortalPage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Page Object representing Employees -> Bulk upload: download the portal CSV template,
 * upload the filled file, and read the summary of rows added / rows rejected with reasons.
 */
public class BulkUploadPage extends PortalPage {

    // "3 rows added", "Added: 3", "3 employees imported successfully", ...
    private static final String COUNT_BEFORE = "(\\d+)\\s+(?:of\\s+\\d+\\s+)?(?:rows?|records?|employees?)?\\s*(?:were\\s+|have\\s+been\\s+)?";
    private static final String COUNT_AFTER = "\\s*(?:rows?|records?|employees?)?\\s*[:\\-]?\\s*(\\d+)";
    private static final Pattern ADDED = Pattern.compile("(?i)" + COUNT_BEFORE + "(?:added|imported|created|uploaded|successful|succeeded)"
            + "|(?:added|imported|created|successful)" + COUNT_AFTER);
    private static final Pattern REJECTED = Pattern.compile("(?i)" + COUNT_BEFORE + "(?:rejected|failed|invalid|errors?)"
            + "|(?:rejected|failed|invalid|errors?)" + COUNT_AFTER);

    private final By downloadTemplateLink = actionWithText("template");
    private final By fileInput = By.cssSelector("input[type='file']");
    private final By uploadSubmitButton = By.xpath("//button[contains(translate(normalize-space(.),'UPLOADIMPRTSBMI','uploadimprtsbmi'),'upload')"
            + " or contains(translate(normalize-space(.),'UPLOADIMPRTSBMI','uploadimprtsbmi'),'import')"
            + " or contains(translate(normalize-space(.),'UPLOADIMPRTSBMI','uploadimprtsbmi'),'submit')] | //form//button[@type='submit']");

    public BulkUploadPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the portal's template link and returns the downloaded CSV file.
     */
    public File downloadTemplate() {
        File dir = DriverFactory.getDownloadDirectory();
        String[] before = dir.list();
        Set<String> existing = new HashSet<>(before == null ? Set.of() : Arrays.asList(before));
        click(downloadTemplateLink);
        return WaitUtils.waitForNewDownload(dir, existing, ConfigReader.getExplicitWait());
    }

    /**
     * Uploads the file by sending its absolute path to input[type='file'] (no OS file dialog),
     * then submits if the page has an explicit upload button.
     */
    public void uploadFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Upload file does not exist at path: " + file.getAbsolutePath());
        }
        // The file input is often visually hidden behind a styled drop zone, so wait for presence only
        WaitUtils.waitForPresence(driver, fileInput, ConfigReader.getExplicitWait());
        driver.findElement(fileInput).sendKeys(file.getAbsolutePath());
        if (isDisplayed(uploadSubmitButton, ConfigReader.getShortWait())) {
            click(uploadSubmitButton);
        }
    }

    /**
     * Result summary of a processed upload.
     */
    public static final class UploadSummary {
        private final int added;
        private final int rejected;
        private final String text;

        UploadSummary(int added, int rejected, String text) {
            this.added = added;
            this.rejected = rejected;
            this.text = text;
        }

        public int getAdded() {
            return added;
        }

        public int getRejected() {
            return rejected;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Waits until the page shows both an "added" and a "rejected" count, and parses them.
     */
    public UploadSummary waitForSummary() {
        String pageText;
        try {
            pageText = WaitUtils.waitUntil(driver, d -> {
                String text = getPageText();
                return ADDED.matcher(text).find() && REJECTED.matcher(text).find() ? text : null;
            }, ConfigReader.getExplicitWait() * 3);
        } catch (TimeoutException e) {
            throw new AssertionError("No upload summary with added/rejected counts appeared. Page text:\n" + getPageText());
        }
        return new UploadSummary(firstCount(ADDED, pageText), firstCount(REJECTED, pageText), pageText);
    }

    private static int firstCount(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return -1;
        }
        return Integer.parseInt(m.group(1) != null ? m.group(1) : m.group(2));
    }

    /**
     * Returns the text of the rejected-row entry that mentions the given value (e.g. the invalid email),
     * or null if no such row is shown.
     */
    public String getRejectedRowText(String identifyingValue) {
        try {
            return visible(rowContaining(identifyingValue)).getText();
        } catch (TimeoutException e) {
            return Arrays.stream(getPageText().split("\n"))
                    .filter(line -> line.contains(identifyingValue))
                    .collect(Collectors.collectingAndThen(Collectors.joining(" | "), s -> s.isEmpty() ? null : s));
        }
    }
}
