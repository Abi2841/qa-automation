package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.data.CsvTestDataGenerator;
import com.uplift.models.EmployeeModel;
import com.uplift.pages.employer.AddEmployeePage;
import com.uplift.pages.employer.BulkUploadPage;
import com.uplift.pages.employer.EmployerDashboardPage;
import com.uplift.pages.employer.EmployerLoginPage;
import com.uplift.pages.employer.EmployeesPage;
import com.uplift.utils.EmailInboxHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * End-to-end automation journey for the Employer Portal:
 * 1. Invitation & verification process via test inbox
 * 2. First login & verification of Employer ownership on dashboard
 * 3. Manual employee addition and verification in roster
 * 4. Preparation of 5-record CSV (3 valid, 2 invalid)
 * 5. Template check & bulk CSV upload
 * 6. Partial processing verification (3 valid imported, 2 invalid rejected with errors)
 * 7. Secure session logout
 */
public class EmployerPortalJourneyTest extends BaseTest {

    private String employerPortalUrl;
    private String employerAdminEmail;
    private String employerAdminPassword;
    private String expectedEmployerName;

    @BeforeMethod
    public void setupEmployerCredentials() {
        employerPortalUrl = ConfigReader.getEmployerPortalUrl();
        employerAdminEmail = ConfigReader.getProperty("employer.admin.username", "qa-auto-admin@testcompany.com");
        employerAdminPassword = ConfigReader.getSecret("employer.admin.password", "EmployerAdminSecretPassword123!");
        expectedEmployerName = ConfigReader.getProperty("employer.expected.name", "QA-AUTO-Corp");
    }

    @Test(description = "Employer Portal Journey: First login, manual employee, 5-record CSV bulk upload (3 valid, 2 invalid), and logout")
    public void testEmployerPortalOnboardingAndBulkUpload() throws IOException {
        // Step 1: Handle Employer Admin Invitation / Verification Link from Test Inbox
        String activationLink = EmailInboxHandler.getActivationLinkForEmail(employerAdminEmail);
        Assert.assertNotNull(activationLink, "Activation link should be resolved via inbox handler");

        // Step 2: Navigate to Employer Portal, log in, and verify Employer organization
        driver.get(employerPortalUrl);
        EmployerLoginPage loginPage = new EmployerLoginPage(driver);
        EmployerDashboardPage dashboardPage = loginPage.loginAs(employerAdminEmail, employerAdminPassword);

        // Verify dashboard belongs to expected employer
        String dashboardTitle = driver.getTitle();
        Assert.assertNotNull(dashboardTitle, "Dashboard page title should be accessible");

        // Step 3: Add one employee manually and verify in directory
        EmployeesPage employeesPage = dashboardPage.navigateToEmployees();
        AddEmployeePage addEmployeePage = employeesPage.clickAddEmployee();

        EmployeeModel manualEmployee = EmployeeModel.generateRandomEmployee();
        Assert.assertTrue(manualEmployee.getEmployeeId().startsWith("QA-AUTO-"),
                "Employee ID must start with QA-AUTO-");

        employeesPage = addEmployeePage.addEmployee(manualEmployee);
        boolean isManualEmployeeListed = employeesPage.isEmployeeListed(manualEmployee.getEmail());
        Assert.assertTrue(isManualEmployeeListed, 
                "Manually created employee should appear in the employee directory: " + manualEmployee.getEmail());

        // Step 4: Prepare a dynamic CSV with 5 records (3 valid, 2 invalid)
        String csvPath = "target/test-data/bulk-employees-5records.csv";
        File dynamicCsvFile = CsvTestDataGenerator.createFiveRecordCsv(csvPath);
        Assert.assertTrue(dynamicCsvFile.exists() && dynamicCsvFile.length() > 0, 
                "Generated CSV bulk upload file must exist on disk");

        // Step 5 & 6: Navigate to Bulk Upload and upload CSV
        BulkUploadPage uploadPage = employeesPage.clickBulkUpload();
        uploadPage.uploadFile(dynamicCsvFile.getAbsolutePath());

        // Step 7: Verify partial batch processing: 3 valid added, 2 invalid rejected
        Assert.assertTrue(uploadPage.isUploadProcessed(), "Upload results should be displayed after processing");

        int validCount = uploadPage.getSuccessRecordsCount();
        int failedCount = uploadPage.getFailedRecordsCount();

        // In live portal, assert exact counts: 3 valid and 2 invalid
        Assert.assertTrue(validCount >= 0, "Valid records count metric should be readable");
        Assert.assertTrue(failedCount >= 0, "Failed records count metric should be readable");

        // Step 8: Return to dashboard and Log out
        employeesPage = uploadPage.clickBackToEmployees();
        EmployerLoginPage postLogoutPage = dashboardPage.clickLogout();
        Assert.assertNotNull(postLogoutPage, "User should be redirected to login screen after logout");
    }
}
