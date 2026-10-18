package com.uplift.tests;

import com.aventstack.extentreports.ExtentReports;
import com.fasterxml.jackson.databind.JsonNode;
import com.uplift.config.ConfigReader;
import com.uplift.data.TestDataManager;
import com.uplift.reporting.ExtentManager;
import com.uplift.reporting.ExtentTestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Framework verification smoke test to validate that configuration,
 * test data loading, and reporting utilities function correctly.
 */
@Listeners(ExtentTestListener.class)
public class FrameworkSmokeTest {

    @Test(description = "Verify configuration reader loads QA environment properties correctly")
    public void testConfigurationReaderLoadsDefaults() {
        String baseUrl = ConfigReader.getBaseUrl();
        String apiBaseUrl = ConfigReader.getApiBaseUrl();
        String browser = ConfigReader.getBrowser();
        int implicitWait = ConfigReader.getImplicitWait();

        Assert.assertNotNull(baseUrl, "Base URL should not be null");
        Assert.assertFalse(baseUrl.isEmpty(), "Base URL should not be empty");
        Assert.assertNotNull(apiBaseUrl, "API Base URL should not be null");
        Assert.assertNotNull(browser, "Browser should not be null");
        Assert.assertTrue(implicitWait > 0, "Implicit wait should be greater than 0");
    }

    @Test(description = "Verify test data manager loads JSON fixtures")
    public void testTestDataManagerLoadsJsonFixture() {
        JsonNode fixture = TestDataManager.getJsonNode("testdata/sample-data.json");
        Assert.assertNotNull(fixture, "Sample test data fixture should not be null");

        JsonNode userNode = fixture.get("smokeUser");
        Assert.assertNotNull(userNode, "smokeUser object should exist in sample-data.json");
        Assert.assertEquals(userNode.get("username").asText(), "test_qa_user");
    }

    @Test(description = "Verify ExtentReports instance initializes with framework metadata")
    public void testExtentReportsInitialization() {
        ExtentReports reports = ExtentManager.getExtentReports();
        Assert.assertNotNull(reports, "ExtentReports instance should be initialized");
    }
}
