package com.uplift.api;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployeeModel;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Object encapsulating HTTP interactions with Employee and Auth endpoints.
 * Analogous to Page Object Model, but for REST API resources.
 */
public final class EmployeeApiService {

    private EmployeeApiService() {
        // Prevent direct instantiation
    }

    private static String getAuthEndpoint() {
        return ConfigReader.getProperty("api.auth.endpoint", "/login");
    }

    private static String getEmployeesEndpoint() {
        return ConfigReader.getProperty("api.employees.endpoint", "/users");
    }

    /**
     * Requirement 1: Obtains authentication token using documented credentials.
     */
    public static Response postAuthLogin(String username, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("email", username);
        payload.put("password", password);

        return RestAssured.given()
                .spec(ApiSpecFactory.getDefaultRequestSpec())
                .body(payload)
                .when()
                .post(getAuthEndpoint())
                .then()
                .extract()
                .response();
    }

    /**
     * Requirement 2: Sends employee creation request without an Authorization token.
     */
    public static Response createEmployeeWithoutToken(EmployeeModel employee) {
        return RestAssured.given()
                .spec(ApiSpecFactory.getDefaultRequestSpec())
                .body(employee)
                .when()
                .post(getEmployeesEndpoint())
                .then()
                .extract()
                .response();
    }

    /**
     * Requirement 3: Creates an employee using POST with an authenticated Bearer token.
     */
    public static Response createEmployee(EmployeeModel employee, String token) {
        return RestAssured.given()
                .spec(ApiSpecFactory.getAuthenticatedRequestSpec(token))
                .body(employee)
                .when()
                .post(getEmployeesEndpoint())
                .then()
                .extract()
                .response();
    }

    /**
     * Requirement 4: Retrieves an employee by ID using GET.
     */
    public static Response getEmployeeById(String employeeId, String token) {
        return RestAssured.given()
                .spec(ApiSpecFactory.getAuthenticatedRequestSpec(token))
                .when()
                .get(getEmployeesEndpoint() + "/" + employeeId)
                .then()
                .extract()
                .response();
    }

    /**
     * Requirement 5: Submits an employee record with an invalid PPSN.
     */
    public static Response createEmployeeWithInvalidPpsn(EmployeeModel validBase, String invalidPpsn, String token) {
        validBase.setPpsn(invalidPpsn);
        return RestAssured.given()
                .spec(ApiSpecFactory.getAuthenticatedRequestSpec(token))
                .body(validBase)
                .when()
                .post(getEmployeesEndpoint())
                .then()
                .extract()
                .response();
    }
}
