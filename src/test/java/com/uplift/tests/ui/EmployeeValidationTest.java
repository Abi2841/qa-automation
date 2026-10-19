package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.data.EmployeeValidationDataProvider;
import com.uplift.data.JourneyContext;
import com.uplift.models.EmployeeModel;
import com.uplift.pages.employer.AddEmployeePage;
import com.uplift.pages.employer.EmployerDashboardPage;
import com.uplift.pages.employer.EmployerLoginPage;
import com.uplift.reporting.ReportLogger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Part C: ONE data-driven test covering the Add employee form validations:
 * mandatory fields empty, invalid email, invalid Irish PPSN, invalid mobile,
 * duplicate employee (same PPSN or email) and date of birth under 18.
 *
 * Every scenario starts from a fresh valid employee and breaks exactly one rule (see the JSON data file),
 * then asserts that the submit is blocked with a validation message.
 */
public class EmployeeValidationTest extends BaseTest {

    private EmployerDashboardPage dashboardPage;
    private EmployeeModel seedEmployee;

    /**
     * Logs in once as the employer admin (the one created by the journey in this run, or the configured one)
     * and creates the existing employee that the duplicate scenarios collide with.
     */
    @BeforeClass(alwaysRun = true)
    public void loginAndCreateSeedEmployee() {
        EmployerLoginPage loginPage = new EmployerLoginPage(driver).open(ConfigReader.getEmployerPortalUrl());
        dashboardPage = loginPage.loginAs(JourneyContext.employerAdminEmail(), JourneyContext.employerAdminPassword());
        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Employer dashboard should load for the validation suite");

        seedEmployee = EmployeeModel.generateRandomEmployee();
        AddEmployeePage addPage = dashboardPage.navigateToEmployees().clickAddEmployee();
        addPage.addEmployee(seedEmployee);
        Assert.assertTrue(dashboardPage.navigateToEmployees().isEmployeeListed(seedEmployee.getEmail()),
                "Seed employee for the duplicate scenarios should be created: " + seedEmployee.getEmail());
    }

    @Test(dataProvider = "addEmployeeValidationScenarios", dataProviderClass = EmployeeValidationDataProvider.class,
            description = "C: Add employee form rejects invalid data (data-driven from employee-validation-scenarios.json)")
    public void addEmployeeRejectsInvalidData(String scenarioId, String description,
                                              Map<String, String> overrides, String expectedMessageContains) {
        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        overrides.forEach((field, value) -> applyOverride(employee, field, resolve(value)));
        ReportLogger.info(scenarioId + " data: " + employee);

        AddEmployeePage addPage = dashboardPage.navigateToEmployees().clickAddEmployee();
        addPage.fillEmployeeForm(employee);
        addPage.clickSave();

        List<String> messages = addPage.getValidationMessages(ConfigReader.getExplicitWait());
        ReportLogger.info(scenarioId + " validation messages: " + messages);
        Assert.assertFalse(messages.isEmpty(), scenarioId + " (" + description + "): a validation message should be shown");
        Assert.assertTrue(addPage.isFormStillDisplayed(),
                scenarioId + " (" + description + "): the form should stay open because the employee must not be saved");
        if (expectedMessageContains != null) {
            Assert.assertTrue(messages.stream().anyMatch(m -> m.toLowerCase().contains(expectedMessageContains.toLowerCase())),
                    scenarioId + ": expected a message containing '" + expectedMessageContains + "' but got " + messages);
        }
    }

    /**
     * Resolves the dynamic tokens used in the JSON data file.
     */
    private String resolve(String value) {
        if (value.equals("{{seed.email}}")) {
            return seedEmployee.getEmail();
        }
        if (value.equals("{{seed.ppsn}}")) {
            return seedEmployee.getPpsn();
        }
        if (value.startsWith("{{dob.age:")) {
            return LocalDate.now().minusYears(tokenNumber(value)).toString();
        }
        if (value.startsWith("{{dob.days.until.18:")) {
            return LocalDate.now().plusDays(tokenNumber(value)).minusYears(18).toString();
        }
        return value;
    }

    /**
     * Number after the colon in a token such as {{dob.age:16}}.
     */
    private static int tokenNumber(String token) {
        return Integer.parseInt(token.substring(token.lastIndexOf(':') + 1, token.length() - 2));
    }

    private static void applyOverride(EmployeeModel employee, String field, String value) {
        switch (field) {
            case "firstName": employee.setFirstName(value); break;
            case "lastName": employee.setLastName(value); break;
            case "email": employee.setEmail(value); break;
            case "mobileNumber": employee.setMobileNumber(value); break;
            case "dateOfBirth": employee.setDateOfBirth(value); break;
            case "ppsn": employee.setPpsn(value); break;
            case "startDate": employee.setStartDate(value); break;
            default: throw new IllegalArgumentException("Unknown employee field in test data: " + field);
        }
    }
}
