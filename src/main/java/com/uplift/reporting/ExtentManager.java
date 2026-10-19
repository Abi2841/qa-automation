package com.uplift.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.uplift.config.ConfigReader;

import java.io.File;

/**
 * ThreadSafe manager for ExtentReports and ExtentTest instances.
 */
public final class ExtentManager {

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    private ExtentManager() {
        // Prevent direct instantiation
    }

    /**
     * Initializes and returns the singleton ExtentReports instance.
     */
    public static synchronized ExtentReports getExtentReports() {
        if (extentReports == null) {
            String reportPath = ConfigReader.getReportPath();
            File reportDir = new File(reportPath).getParentFile();
            if (reportDir != null && !reportDir.exists()) {
                reportDir.mkdirs();
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle(ConfigReader.getProperty("report.title", "QA Automation Report"));
            sparkReporter.config().setReportName(ConfigReader.getProperty("report.name", "Execution Results"));
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Environment", ConfigReader.getEnvironment());
            extentReports.setSystemInfo("Target Browser", ConfigReader.getBrowser());
            extentReports.setSystemInfo("Headless", String.valueOf(ConfigReader.isHeadless()));
            // getProperty (not the required accessors) so an unset URL never breaks report creation
            for (String key : new String[]{"admin.portal.url", "employer.portal.url", "api.base.url", "base.url"}) {
                String value = ConfigReader.getProperty(key);
                if (value != null) {
                    extentReports.setSystemInfo(key, value);
                }
            }
        }
        return extentReports;
    }

    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    public static void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }

    public static void unloadTest() {
        extentTestThreadLocal.remove();
    }

    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
