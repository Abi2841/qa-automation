package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.data.CsvTestDataGenerator;
import com.uplift.data.JourneyContext;
import com.uplift.data.TestDataGenerator;
import com.uplift.models.EmployeeModel;
import com.uplift.models.EmployerAdminModel;
import com.uplift.models.EmployerModel;
import com.uplift.pages.employer.AddEmployeePage;
import com.uplift.pages.employer.BulkUploadPage;
import com.uplift.pages.employer.EmployerActivationPage;
import com.uplift.pages.employer.EmployerDashboardPage;
import com.uplift.pages.employer.EmployerLoginPage;
import com.uplift.pages.employer.EmployeesPage;
import com.uplift.reporting.ReportLogger;
import com.uplift.utils.EmailInboxHandler;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Part B, Employer portal (application guide Steps 3-4), continuing from AdminPortalJourneyTest:
 * 4. First login from the invite email (verification code + set password), dashboard for that employer only,
 *    then log in again with the new password
 * 5. Add one employee manually and verify they appear in the employee list
 * 6. Bulk-upload a CSV built from the portal template: 5 rows (3 valid, 2 invalid); verify 3 added,
 *    2 rejected with a clear reason each, valid employees listed and invalid ones not listed
 * 7. Log out (and verify the session is ended)
 * Bonus (Part F): the manually added employee receives the introduction email.
 */
public class EmployerPortalJourneyTest extends BaseTest {

    public static final String GROUP = "employer-portal-journey";

    private EmployerDashboardPage dashboardPage;
    private String dashboardUrl;
    private Instant employeeAddedAt;

    @Test(groups = GROUP, priority = 1, dependsOnGroups = AdminPortalJourneyTest.GROUP,
            description = "B4a: Employer admin opens the invite email, enters the verification code and sets a password")
    public void employerAdminActivatesAccountFromInvite() {
        EmployerAdminModel admin = JourneyContext.getEmployerAdmin();
        EmployerModel employer = JourneyContext.getEmployer();

        EmailInboxHandler.Email invite = EmailInboxHandler.waitForEmail(admin.getEmail(), JourneyContext.getAdminInviteSentAt());
        ReportLogger.info("Invite email received: '" + invite.getSubject() + "'");
        String code = EmailInboxHandler.extractVerificationCode(invite);
        String link = EmailInboxHandler.extractLink(invite, ConfigReader.getEmployerPortalUrl());

        String password = TestDataGenerator.strongPassword();
        dashboardPage = new EmployerActivationPage(driver).open(link).activate(code, password);
        JourneyContext.setEmployerAdminPassword(password);

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Employer dashboard should load after setting the password");
        Assert.assertTrue(dashboardPage.isDashboardForEmployer(employer.getCompanyName()),
                "Dashboard should belong to the new employer: " + employer.getCompanyName());
    }

    @Test(groups = GROUP, priority = 2, dependsOnMethods = "employerAdminActivatesAccountFromInvite",
            description = "B4b: Employer admin logs in to the Employer portal with the new password")
    public void employerAdminLogsIn() {
        dashboardPage.clickLogout();
        EmployerLoginPage loginPage = new EmployerLoginPage(driver).open(ConfigReader.getEmployerPortalUrl());
        dashboardPage = loginPage.loginAs(JourneyContext.employerAdminEmail(), JourneyContext.employerAdminPassword());

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Employer dashboard should load after login");
        Assert.assertTrue(dashboardPage.isDashboardForEmployer(JourneyContext.getEmployer().getCompanyName()),
                "Dashboard should show only the admin's own employer");
        dashboardUrl = driver.getCurrentUrl();
    }

    @Test(groups = GROUP, priority = 3, dependsOnMethods = "employerAdminLogsIn",
            description = "B5: Add one employee manually and verify they appear in the employee list")
    public void addEmployeeManually() {
        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        ReportLogger.info("Employee: " + employee);

        AddEmployeePage addPage = dashboardPage.navigateToEmployees().clickAddEmployee();
        employeeAddedAt = Instant.now();
        addPage.addEmployee(employee);
        ReportLogger.info("Success message: " + addPage.waitForSuccessMessage(ConfigReader.getExplicitWait()));

        EmployeesPage employeesPage = dashboardPage.navigateToEmployees();
        Assert.assertTrue(employeesPage.isEmployeeListed(employee.getEmail()),
                "Manually added employee should appear in the employee list: " + employee.getEmail());
        JourneyContext.setManualEmployee(employee);
    }

    @Test(groups = GROUP, priority = 4, dependsOnMethods = "employerAdminLogsIn",
            description = "B6: Bulk upload from the portal CSV template - 3 valid rows added, 2 invalid rows rejected with reasons")
    public void bulkUploadThreeValidTwoInvalid() throws IOException {
        EmployeeModel valid1 = EmployeeModel.generateRandomEmployee();
        EmployeeModel valid2 = EmployeeModel.generateRandomEmployee();
        EmployeeModel valid3 = EmployeeModel.generateRandomEmployee();
        // Exactly one fault per invalid row, so each rejection has a single expected reason
        EmployeeModel invalidEmail = EmployeeModel.generateRandomEmployee();
        invalidEmail.setEmail("qa-auto-invalid-email-" + TestDataGenerator.uniqueId());
        EmployeeModel invalidPpsn = EmployeeModel.generateRandomEmployee();
        invalidPpsn.setPpsn("12345AB");
        List<EmployeeModel> rows = Arrays.asList(valid1, valid2, valid3, invalidEmail, invalidPpsn);

        BulkUploadPage uploadPage = dashboardPage.navigateToEmployees().clickBulkUpload();
        File template = uploadPage.downloadTemplate();
        List<String> header = CsvTestDataGenerator.readTemplateHeader(template);
        ReportLogger.info("Portal template columns: " + header);
        File csv = CsvTestDataGenerator.writeCsv(header, rows,
                "target/test-data/bulk-upload-" + TestDataGenerator.uniqueId() + ".csv");
        ReportLogger.info("Uploading " + csv.getName() + ": rows 1-3 valid, row 4 invalid email ("
                + invalidEmail.getEmail() + "), row 5 invalid PPSN (" + invalidPpsn.getPpsn() + ")");

        uploadPage.uploadFile(csv);
        BulkUploadPage.UploadSummary summary = uploadPage.waitForSummary();
        Assert.assertEquals(summary.getAdded(), 3, "Rows added in the upload summary");
        Assert.assertEquals(summary.getRejected(), 2, "Rows rejected in the upload summary");

        assertRejectedWithReason(uploadPage, 4, "email", invalidEmail.getEmail(), invalidEmail.getEmail());
        assertRejectedWithReason(uploadPage, 5, "pps", invalidPpsn.getPpsn(), invalidPpsn.getEmail());

        EmployeesPage employeesPage = dashboardPage.navigateToEmployees();
        for (EmployeeModel valid : Arrays.asList(valid1, valid2, valid3)) {
            Assert.assertTrue(employeesPage.isEmployeeListed(valid.getEmail()),
                    "Valid bulk row should be added to the employee list: " + valid.getEmail());
        }
        for (EmployeeModel invalid : Arrays.asList(invalidEmail, invalidPpsn)) {
            Assert.assertFalse(employeesPage.isEmployeeListed(invalid.getEmail(), ConfigReader.getShortWait()),
                    "Rejected bulk row must not be added: " + invalid.getEmail());
        }
    }

    /**
     * The rejected row must be shown with a reason that names the faulty field.
     * Rows are looked up by their values first, then by row/line number (with or without the header row).
     */
    private void assertRejectedWithReason(BulkUploadPage page, int dataRow, String fieldKeyword, String... values) {
        String rowText = null;
        for (String key : new String[]{values[0], values.length > 1 ? values[1] : values[0],
                "Row " + dataRow, "Row " + (dataRow + 1), "Line " + dataRow, "Line " + (dataRow + 1)}) {
            rowText = page.getRejectedRowText(key);
            if (rowText != null) {
                break;
            }
        }
        Assert.assertNotNull(rowText, "Rejected row " + dataRow + " should be listed with its reason");
        ReportLogger.info("Rejected row " + dataRow + ": " + rowText);
        Assert.assertTrue(rowText.toLowerCase().contains(fieldKeyword),
                "Reason for rejected row " + dataRow + " should mention '" + fieldKeyword + "': " + rowText);
    }

    @Test(groups = GROUP, priority = 5, dependsOnMethods = "addEmployeeManually",
            description = "Bonus F: The manually added employee receives the introduction email inviting them to the app")
    public void employeeReceivesIntroductionEmail() {
        if (!Boolean.parseBoolean(ConfigReader.getProperty("verify.employee.invite.email", "true"))) {
            throw new SkipException("verify.employee.invite.email=false");
        }
        EmployeeModel employee = JourneyContext.getManualEmployee();
        if (!employee.getEmail().contains("+")) {
            throw new SkipException("Employee email is not a test-inbox alias (inbox.address not configured)");
        }
        EmailInboxHandler.Email email = EmailInboxHandler.waitForEmail(employee.getEmail(), employeeAddedAt);
        ReportLogger.info("Introduction email received: '" + email.getSubject() + "'");
        Assert.assertFalse(email.getPlainText().trim().isEmpty(), "Introduction email should have content");
    }

    @Test(groups = GROUP, priority = 6, dependsOnMethods = "employerAdminLogsIn",
            description = "B7: Employer admin logs out; the dashboard is no longer reachable without logging in")
    public void employerAdminLogsOut() {
        EmployerLoginPage loginPage = dashboardPage.clickLogout();
        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "Login page should be shown after logout");

        driver.get(dashboardUrl);
        Assert.assertTrue(new EmployerLoginPage(driver).isLoginPageDisplayed(),
                "Opening the dashboard URL after logout should require logging in again");
    }
}
