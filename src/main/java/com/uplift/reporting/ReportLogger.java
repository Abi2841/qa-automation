package com.uplift.reporting;

import com.aventstack.extentreports.ExtentTest;

/**
 * Writes step-level messages to the current ExtentReports test node and to the console log.
 * Never pass passwords, tokens or verification codes to these methods.
 */
public final class ReportLogger {

    private ReportLogger() {
        // Prevent direct instantiation
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.info(message);
        }
    }

    public static void warning(String message) {
        System.out.println("[WARN] " + message);
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.warning(message);
        }
    }
}
