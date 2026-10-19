package com.uplift.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.uplift.base.DriverFactory;
import com.uplift.config.ConfigReader;
import com.uplift.utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener to automatically log test lifecycle events into ExtentReports
 * and attach failure screenshots (embedded in the report and saved to reports/screenshots
 * so CI can archive them as build artifacts).
 */
public class ExtentTestListener implements ITestListener {

    private static final ExtentReports extent = ExtentManager.getExtentReports();

    @Override
    public void onStart(ITestContext context) {
        // Report initialized on first invocation
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flushReports();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = buildTestName(result);
        String description = result.getMethod().getDescription();
        ExtentTest test = extent.createTest(testName, description != null ? description : "");

        // Tag test with its class and groups
        test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
        for (String group : result.getMethod().getGroups()) {
            test.assignCategory(group);
        }
        test.log(Status.INFO, "Started execution: " + testName);
        ExtentManager.setTest(test);
    }

    /**
     * Data-driven iterations get their first two parameters (scenario id and description) in the name,
     * so each row of the data file is identifiable in the report.
     */
    private static String buildTestName(ITestResult result) {
        String name = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        if (params != null && params.length >= 2) {
            name += " [" + params[0] + " - " + params[1] + "]";
        } else if (params != null && params.length == 1) {
            name += " [" + params[0] + "]";
        }
        return name;
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed: " + result.getMethod().getMethodName());
        }
        ExtentManager.unloadTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + result.getMethod().getMethodName());
            if (result.getThrowable() != null) {
                test.log(Status.FAIL, result.getThrowable());
            }

            // Capture screenshot if UI driver is active
            if (ConfigReader.isScreenshotOnFailure()) {
                WebDriver driver = DriverFactory.getDriver();
                if (driver != null) {
                    String file = ScreenshotUtils.captureFile(driver, buildTestName(result));
                    String base64 = ScreenshotUtils.captureBase64(driver);
                    if (base64 != null) {
                        test.fail("Failure screenshot" + (file != null ? " (saved as reports/" + file + ")" : "")
                                + " - URL: " + driver.getCurrentUrl(),
                                MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                    }
                }
            }
        }
        ExtentManager.unloadTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test == null) {
            // Skipped because a dependency or configuration method failed: onTestStart was never called
            test = extent.createTest(buildTestName(result));
        }
        test.log(Status.SKIP, "Test skipped: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            test.log(Status.SKIP, result.getThrowable());
        }
        ExtentManager.unloadTest();
    }
}
