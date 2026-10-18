package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.models.EmployerAdminModel;
import com.uplift.models.EmployerModel;
import com.uplift.pages.admin.AdminDashboardPage;
import com.uplift.pages.admin.AdminLoginPage;
import com.uplift.pages.admin.CreateEmployerPage;
import com.uplift.pages.admin.EmployerDetailsPage;
import com.uplift.pages.admin.EmployersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * End-to-end automation journey for the Super Admin Portal:
 * 1. Admin login with secure credentials
 * 2. Navigate to Employers & open creation form
 * 3. Generate dynamic test data prefixed with 'QA-AUTO-'
 * 4. Populate company details, CRN, address, payroll date, and bank details
 * 5. Save and assert success notification & listing presence
 * 6. Open the created employer & provision an Employer Admin
 * 7. Assert Employer Admin presence in the company admin roster
 */
public class AdminPortalJourneyTest extends BaseTest {

    private String adminUrl;
    private String adminUsername;
    private String adminPassword;

    @BeforeMethod
    public void setupAdminCredentials() {
        adminUrl = ConfigReader.getAdminPortalUrl();
        adminUsername = ConfigReader.getAdminUsername();
        adminPassword = ConfigReader.getAdminPassword();
    }

    @Test(description = "Admin Journey: Onboard a new Employer and provision an Employer Admin")
    public void testAdminCreatesEmployerAndEmployerAdmin() {
        // Step 1: Navigate to Admin Portal and Log in
        driver.get(adminUrl);
        AdminLoginPage loginPage = new AdminLoginPage(driver);

        // Verify login page is reachable
        Assert.assertNotNull(driver.getTitle(), "Admin portal page title should be readable");

        AdminDashboardPage dashboardPage = loginPage.loginAs(adminUsername, adminPassword);

        // Step 2: Navigate to Employers section
        EmployersPage employersPage = dashboardPage.navigateToEmployers();

        // Step 3: Generate unique dynamic Employer test data with QA-AUTO- prefix
        EmployerModel employerData = EmployerModel.generateRandomEmployer();
        Assert.assertTrue(employerData.getCompanyName().startsWith("QA-AUTO-"),
                "Employer name must be prefixed with QA-AUTO-");

        // Step 4: Open form and enter Company Details, CRN, Address, Payroll Date, Bank Details
        CreateEmployerPage createPage = employersPage.clickCreateEmployer();
        createPage.createEmployer(employerData);

        // Step 5: Save and verify success notification & listing presence
        boolean isListed = employersPage.isEmployerListed(employerData.getCompanyName());
        Assert.assertTrue(isListed, "Newly created employer should be displayed in the Employers table: " 
                + employerData.getCompanyName());

        // Step 6: Open the created Employer and provision an Employer Admin
        EmployerDetailsPage detailsPage = employersPage.openEmployer(employerData.getCompanyName());
        EmployerAdminModel adminData = EmployerAdminModel.generateRandomEmployerAdmin();
        Assert.assertTrue(adminData.getFirstName().startsWith("QA-AUTO-"),
                "Employer Admin first name must be prefixed with QA-AUTO-");

        detailsPage.addEmployerAdmin(adminData);

        // Step 7: Verify Employer Admin is listed in the roster
        boolean isAdminListed = detailsPage.isAdminListed(adminData.getEmail());
        Assert.assertTrue(isAdminListed, "Employer Admin should appear in the admin roster: " 
                + adminData.getEmail());
    }
}
