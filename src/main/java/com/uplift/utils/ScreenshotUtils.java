package com.uplift.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Reusable utility for capturing and storing WebDriver screenshots.
 */
public final class ScreenshotUtils {

    private static final String SCREENSHOT_DIR = "reports/screenshots/";

    private ScreenshotUtils() {
        // Prevent direct instantiation
    }

    /**
     * Captures a screenshot as Base64 format for inline HTML report embedding.
     */
    public static String captureBase64(WebDriver driver) {
        if (driver == null) {
            return null;
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.err.println("[WARN] Unable to capture base64 screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot file on disk and returns its saved path.
     */
    public static String captureFile(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        try {
            File dir = new File(SCREENSHOT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String sanitizedName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = sanitizedName + "_" + timestamp + ".png";
            File targetFile = new File(dir, fileName);

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return "screenshots/" + fileName;
        } catch (IOException e) {
            System.err.println("[WARN] Failed to save screenshot file: " + e.getMessage());
            return null;
        }
    }
}
