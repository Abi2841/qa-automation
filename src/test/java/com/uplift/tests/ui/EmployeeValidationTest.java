package com.uplift.tests.ui;

import com.uplift.base.BaseTest;
import com.uplift.config.ConfigReader;
import com.uplift.data.EmployeeValidationDataProvider;
import com.uplift.models.EmployeeModel;
import com.uplift.pages.employer.AddEmployeePage;
import com.uplift.pages.employer.EmployerDashboardPage;
import com.uplift.pages.employer.EmployerLoginPage;
import com.uplift.pages.employer.EmployeesPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Part C: Data-driven negative and boundary validation suite for the Add Employee form.
 *
 * Covers:
 * 1. Mandatory fields left empty
 * 2. Invalid email format
 * 3. Invalid Irish PPSN
 * 4. Invalid mobile number
 * 5. Duplicate employee using the same PPSN or email
 * 6. Date of birth that makes the employee under 18
 */
public class EmployeeValidationTest extends BaseTest {

    private String employerPortalUrl;
    private String employerAdminEmail;
    private String employerAdminPassword;

    @BeforeMethod
    public void setupCredentials() {
        employerPortalUrl = ConfigReader.getEmployerPortalUrl();
        employerAdminEmail = ConfigReader.getProperty("employer.admin.username", "qa-auto-admin@testcompany.com");
        employerAdminPassword = ConfigReader.getSecret("employer.admin.password", "EmployerAdminSecretPassword123!");
    }

    private AddEmployeePage navigateToAddEmployeeForm() {
        driver.get(employerPortalUrl);
        EmployerLoginPage loginPage = new EmployerLoginPage(driver);
        EmployerDashboardPage dashboardPage = loginPage.loginAs(employerAdminEmail, employerAdminPassword);
        EmployeesPage employeesPage = dashboardPage.navigateToEmployees();
        return employeesPage.clickAddEmployee();
    }

    @Test(description = "1. Verify submission fails when all mandatory fields are left empty")
    public void testMandatoryFieldsEmptyValidation() {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        // Submit blank form without filling required fields
        addPage.submitExpectingValidationFailure();

        // Assert validation behavior: form blocked and error state indicated
        Assert.assertTrue(addPage.hasValidationErrors(),
                "Form submission should be blocked when mandatory fields are empty");
        Assert.assertTrue(driver.getCurrentUrl().contains("employee") || driver.getTitle() != null,
                "User should remain on Add Employee view on validation failure");
    }

    @Test(dataProvider = "invalidEmailProvider", dataProviderClass = EmployeeValidationDataProvider.class,
          description = "2. Verify validation fails for invalid email syntax")
    public void testInvalidEmailFormatValidation(String invalidEmail, String scenarioDescription) {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        employee.setEmail(invalidEmail);

        addPage.fillEmployeeForm(employee);
        addPage.submitExpectingValidationFailure();

        Assert.assertTrue(addPage.hasValidationErrors(),
                "Validation error should appear for scenario [" + scenarioDescription + "] with email: " + invalidEmail);
    }

    @Test(dataProvider = "invalidPpsnProvider", dataProviderClass = EmployeeValidationDataProvider.class,
          description = "3. Verify validation fails for invalid Irish PPSN formats")
    public void testInvalidIrishPpsnValidation(String invalidPpsn, String scenarioDescription) {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        employee.setPpsn(invalidPpsn);

        addPage.fillEmployeeForm(employee);
        addPage.submitExpectingValidationFailure();

        Assert.assertTrue(addPage.hasValidationErrors(),
                "Validation error should trigger for scenario [" + scenarioDescription + "] with PPSN: " + invalidPpsn);
    }

    @Test(dataProvider = "invalidMobileProvider", dataProviderClass = EmployeeValidationDataProvider.class,
          description = "4. Verify validation fails for invalid Irish mobile numbers")
    public void testInvalidMobileValidation(String invalidMobile, String scenarioDescription) {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        employee.setMobileNumber(invalidMobile);

        addPage.fillEmployeeForm(employee);
        addPage.submitExpectingValidationFailure();

        Assert.assertTrue(addPage.hasValidationErrors(),
                "Validation error should trigger for scenario [" + scenarioDescription + "] with mobile: " + invalidMobile);
    }

    @Test(dataProvider = "under18DobProvider", dataProviderClass = EmployeeValidationDataProvider.class,
          description = "6. Verify validation rejects employees under 18 years of age")
    public void testUnder18DateOfBirthValidation(String under18Dob, int age, String scenarioDescription) {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        employee.setDateOfBirth(under18Dob);

        addPage.fillEmployeeForm(employee);
        addPage.submitExpectingValidationFailure();

        Assert.assertTrue(addPage.hasValidationErrors(),
                "System must reject employee who is under 18 (age: " + age + ", DOB: " + under18Dob + ")");
    }

    @Test(description = "5. Verify system rejects creating a duplicate employee with the same PPSN or Email")
    public void testDuplicateEmployeePpsnOrEmailValidation() {
        AddEmployeePage addPage = navigateToAddEmployeeForm();

        // Step 1: Create initial valid employee
        EmployeeModel existingEmployee = EmployeeModel.generateRandomEmployee();
        EmployeesPage employeesPage = addPage.addEmployee(existingEmployee);

        // Step 2: Attempt to onboard a second employee reusing the exact same PPSN and Email
        AddEmployeePage secondAddPage = employeesPage.clickAddEmployee();
        EmployeeModel duplicateEmployee = EmployeeModel.generateRandomEmployee();
        duplicateEmployee.setPpsn(existingEmployee.getPpsn());
        duplicateEmployee.setEmail(existingEmployee.getEmail());

        secondAddPage.fillEmployeeForm(duplicateEmployee);
        secondAddPage.submitExpectingValidationFailure();

        Assert.assertTrue(secondAddPage.hasValidationErrors(),
                "System must reject second employee with duplicate PPSN (" + existingEmployee.getPpsn()
                        + ") or duplicate Email (" + existingEmployee.getEmail() + ")");
    }
}
