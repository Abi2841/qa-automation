package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.data.JourneyContext;
import com.uplift.models.EmployerAdminModel;
import com.uplift.models.EmployerModel;
import com.uplift.pages.admin.AdminDashboardPage;
import com.uplift.pages.admin.AdminLoginPage;
import com.uplift.pages.admin.CreateEmployerPage;
import com.uplift.pages.admin.EmployerDetailsPage;
import com.uplift.pages.admin.EmployersPage;
import com.uplift.reporting.ReportLogger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

/**
 * Part B, Admin portal (application guide Steps 1-2):
 * 1. Log in to the Admin portal
 * 2. Create a new employer with unique QA-AUTO- data (name, CRN, address lookup, payroll date, IBAN/BIC)
 * 3. Add an employer admin (name, email, mobile, economic status, occupation, PEP) to that employer
 *
 * Each step is its own @Test so the report shows exactly where the journey stopped; later steps
 * depend on earlier ones and are skipped (not failed) when a prerequisite fails.
 * The created employer and admin are stored in JourneyContext for EmployerPortalJourneyTest.
 */
public class AdminPortalJourneyTest extends BaseTest {

    public static final String GROUP = "admin-portal-journey";

    private AdminDashboardPage dashboardPage;

    @Test(groups = GROUP, priority = 1,
            description = "B1: Admin logs in to the Admin portal and lands on the dashboard")
    public void adminLogsIn() {
        AdminLoginPage loginPage = new AdminLoginPage(driver).open(ConfigReader.getAdminPortalUrl());
        dashboardPage = loginPage.loginAs(ConfigReader.getAdminUsername(), ConfigReader.getAdminPassword());

        Assert.assertTrue(dashboardPage.isDashboardLoaded(),
                "Admin dashboard (Employers menu) should be shown after login. Current URL: " + driver.getCurrentUrl());
    }

    @Test(groups = GROUP, priority = 2, dependsOnMethods = "adminLogsIn",
            description = "B2: Admin creates an employer; success message shown and employer listed with its status")
    public void adminCreatesEmployer() {
        EmployerModel employer = EmployerModel.generateRandomEmployer();
        Assert.assertTrue(employer.getCompanyName().startsWith(ConfigReader.getTestDataPrefix()),
                "Employer name must carry the QA-AUTO- prefix");
        ReportLogger.info("Employer data: " + employer);

        CreateEmployerPage createPage = dashboardPage.navigateToEmployers().clickAddEmployer();
        createPage.createEmployer(employer);

        String successMessage = createPage.waitForSuccessMessage(ConfigReader.getExplicitWait());
        Assert.assertNotNull(successMessage, "A success message should be shown after saving the employer");
        ReportLogger.info("Success message: " + successMessage);

        EmployersPage employersPage = dashboardPage.navigateToEmployers();
        String row = employersPage.getEmployerRowText(employer.getCompanyName());
        Assert.assertNotNull(row, "Employer should appear in the employer list: " + employer.getCompanyName());
        ReportLogger.info("Employer list row: " + row);

        String expectedStatus = ConfigReader.getProperty("employer.expected.status");
        if (expectedStatus != null) {
            Assert.assertTrue(row.toLowerCase().contains(expectedStatus.toLowerCase()),
                    "Employer row should show status '" + expectedStatus + "' but was: " + row);
        } else {
            Assert.assertTrue(row.replace(employer.getCompanyName(), "").trim().length() > 0,
                    "Employer row should show more than the name (e.g. its status): " + row);
        }
        JourneyContext.setEmployer(employer);
    }

    @Test(groups = GROUP, priority = 3, dependsOnMethods = "adminCreatesEmployer",
            description = "B3: Admin adds an employer admin to the new employer; admin is listed")
    public void adminAddsEmployerAdmin() {
        EmployerModel employer = JourneyContext.getEmployer();
        EmployerAdminModel admin = EmployerAdminModel.generateRandomEmployerAdmin();
        Assert.assertTrue(admin.getFirstName().startsWith(ConfigReader.getTestDataPrefix()),
                "Employer admin name must carry the QA-AUTO- prefix");
        ReportLogger.info("Employer admin: " + admin.getFullName() + " <" + admin.getEmail() + ">, mobile "
                + admin.getMobileNumber() + ", economic status " + admin.getEconomicStatus()
                + ", occupation " + admin.getOccupation() + ", PEP " + admin.getPepAnswer());

        EmployerDetailsPage detailsPage = dashboardPage.navigateToEmployers().openEmployer(employer.getCompanyName());
        // Recorded before saving: the invite email for this admin must arrive after this moment
        JourneyContext.setEmployerAdmin(admin);
        JourneyContext.setAdminInviteSentAt(Instant.now());
        detailsPage.addEmployerAdmin(admin);

        String successMessage = detailsPage.waitForSuccessMessage(ConfigReader.getExplicitWait());
        ReportLogger.info("Success message: " + successMessage);
        Assert.assertTrue(detailsPage.isAdminListed(admin.getEmail()),
                "Employer admin should be listed for the employer: " + admin.getEmail());
    }
}
