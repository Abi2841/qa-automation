package com.uplift.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
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
 * and attach failure screenshots.
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
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        ExtentTest test = extent.createTest(testName, description != null ? description : "");
        
        // Tag test with package or groups
        for (String group : result.getMethod().getGroups()) {
            test.assignCategory(group);
        }
        test.log(Status.INFO, "Started execution: " + testName);
        ExtentManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed successfully: " + result.getMethod().getMethodName());
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
                    String base64 = ScreenshotUtils.captureBase64(driver);
                    if (base64 != null) {
                        test.addScreenCaptureFromBase64String(base64, "Failure Screenshot");
                    }
                }
            }
        }
        ExtentManager.unloadTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped: " + result.getMethod().getMethodName());
            if (result.getThrowable() != null) {
                test.log(Status.SKIP, result.getThrowable());
            }
        }
        ExtentManager.unloadTest();
    }
}
