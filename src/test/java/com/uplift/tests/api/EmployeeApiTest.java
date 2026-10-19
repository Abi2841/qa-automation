package com.uplift.tests.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplift.api.EmployeeApiService;
import com.uplift.base.BaseApiTest;
import com.uplift.base.DriverFactory;
import com.uplift.config.ConfigReader;
import com.uplift.data.JourneyContext;
import com.uplift.models.EmployeeModel;
import com.uplift.pages.employer.EmployerDashboardPage;
import com.uplift.pages.employer.EmployerLoginPage;
import com.uplift.reporting.ReportLogger;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Part D: REST Assured API automation against the documented QA API.
 *
 * 1. Auth: get a token with the documented credentials
 * 2. Auth negative: a request without a token returns 401
 * 3. Create employee: POST returns 201 and the response matches the JSON schema
 * 4. Read back: GET the employee and check every field matches what was sent
 * 5. Validation: POST with a bad PPSN returns 400 with a useful error message
 * 6. UI <-> API cross-check: an employee created via the API is visible in the Employer portal
 */
public class EmployeeApiTest extends BaseApiTest {

    private static final String EMPLOYEE_SCHEMA = "schemas/employee-response.schema.json";

    private String authToken;
    private EmployeeModel createdEmployee;
    private String createdId;

    @BeforeClass(alwaysRun = true)
    public void setupAuth() {
        authToken = EmployeeApiService.getToken(
                ConfigReader.getRequired("api.auth.username"), ConfigReader.getRequired("api.auth.password"));
    }

    private static EmployeeModel newEmployee() {
        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        employee.setEmployerId(ConfigReader.getProperty("api.employer.id"));
        return employee;
    }

    @Test(priority = 1, description = "D1: Obtain an authentication token using the documented credentials")
    public void authReturnsToken() {
        Response response = EmployeeApiService.authenticate(
                ConfigReader.getRequired("api.auth.username"), ConfigReader.getRequired("api.auth.password"));

        Assert.assertEquals(response.getStatusCode(), 200, "Auth endpoint status. Body: " + response.asString());
        String token = response.jsonPath().getString(ConfigReader.getProperty("api.auth.token.path", "token"));
        Assert.assertNotNull(token, "Authentication response must contain a token");
        Assert.assertFalse(token.trim().isEmpty(), "Authentication token should not be empty");
    }

    @Test(priority = 2, description = "D2: A request without a token returns HTTP 401 Unauthorized")
    public void requestWithoutTokenReturns401() {
        Response response = EmployeeApiService.createEmployee(newEmployee(), null);

        Assert.assertEquals(response.getStatusCode(), 401,
                "Unauthenticated POST must be rejected with 401. Body: " + response.asString());
    }

    @Test(priority = 3, description = "D3: Create an employee with POST - HTTP 201 and response matches the schema")
    public void createEmployeeReturns201AndMatchesSchema() {
        EmployeeModel employee = newEmployee();
        ReportLogger.info("POST employee: " + employee);

        Response response = EmployeeApiService.createEmployee(employee, authToken);

        Assert.assertEquals(response.getStatusCode(), 201, "Create employee status. Body: " + response.asString());
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(EMPLOYEE_SCHEMA));
        createdId = EmployeeApiService.extractEmployeeId(response);
        Assert.assertNotNull(createdId, "Created employee response must contain an id");
        createdEmployee = employee;
    }

    @Test(priority = 4, dependsOnMethods = "createEmployeeReturns201AndMatchesSchema",
            description = "D4: GET the created employee - every submitted field matches")
    public void getEmployeeMatchesSubmittedData() {
        Response response = EmployeeApiService.getEmployeeById(createdId, authToken);

        Assert.assertEquals(response.getStatusCode(), 200, "GET employee status. Body: " + response.asString());
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(EMPLOYEE_SCHEMA));

        // Compare every field that was sent (same JSON names as the request body)
        @SuppressWarnings("unchecked")
        Map<String, Object> sent = new ObjectMapper().convertValue(createdEmployee, Map.class);
        sent.remove("employerId");
        for (Map.Entry<String, Object> field : sent.entrySet()) {
            Assert.assertEquals(response.jsonPath().getString(field.getKey()), String.valueOf(field.getValue()),
                    "Field '" + field.getKey() + "' returned by GET should match the value sent");
        }
    }

    @Test(priority = 5, description = "D5: POST with an invalid PPSN returns HTTP 400 with a useful error message")
    public void createEmployeeWithInvalidPpsnReturns400() {
        EmployeeModel employee = newEmployee();
        employee.setPpsn("12345AB"); // 5 digits: violates "7 digits followed by 1-2 letters"

        Response response = EmployeeApiService.createEmployee(employee, authToken);

        Assert.assertEquals(response.getStatusCode(), 400, "Invalid PPSN status. Body: " + response.asString());
        String body = response.asString();
        ReportLogger.info("Error response: " + body);
        Assert.assertTrue(body.toLowerCase().contains("pps"),
                "Error message should identify the PPSN field as the problem: " + body);
    }

    @Test(priority = 6, description = "D6: UI/API cross-check - an employee created via the API is visible in the Employer portal")
    public void apiCreatedEmployeeIsVisibleInEmployerPortal() {
        EmployeeModel employee = newEmployee();
        Response response = EmployeeApiService.createEmployee(employee, authToken);
        Assert.assertEquals(response.getStatusCode(), 201, "Create employee status. Body: " + response.asString());
        ReportLogger.info("Created via API: " + employee.getEmail());

        WebDriver driver = DriverFactory.initDriver();
        try {
            EmployerLoginPage loginPage = new EmployerLoginPage(driver).open(ConfigReader.getEmployerPortalUrl());
            EmployerDashboardPage dashboard = loginPage.loginAs(
                    JourneyContext.employerAdminEmail(), JourneyContext.employerAdminPassword());
            Assert.assertTrue(dashboard.navigateToEmployees().isEmployeeListed(employee.getEmail()),
                    "Employee created via the API should be listed in the Employer portal: " + employee.getEmail());
        } finally {
            DriverFactory.quitDriver();
        }
    }
}
