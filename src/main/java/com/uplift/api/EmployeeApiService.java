package com.uplift.api;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployeeModel;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Object encapsulating HTTP interactions with the Auth and Employee endpoints.
 * Analogous to Page Object Model, but for REST API resources: tests never build requests themselves.
 *
 * Endpoint paths and payload field names come from configuration (environments/qa.properties),
 * so they can be aligned with the provided Swagger without code changes.
 */
public final class EmployeeApiService {

    private EmployeeApiService() {
        // Prevent direct instantiation
    }

    private static String getEmployeesEndpoint() {
        return ConfigReader.getRequired("api.employees.endpoint");
    }

    /**
     * Requests an authentication token with the documented credentials.
     */
    public static Response authenticate(String username, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put(ConfigReader.getProperty("api.auth.username.field", "email"), username);
        payload.put(ConfigReader.getProperty("api.auth.password.field", "password"), password);

        return RestAssured.given()
                .spec(ApiSpecFactory.getDefaultRequestSpec())
                .body(payload)
                .when()
                .post(ConfigReader.getRequired("api.auth.endpoint"))
                .then()
                .extract()
                .response();
    }

    /**
     * Reusable helper: returns a token or fails with the status and body of the auth response.
     */
    public static String getToken(String username, String password) {
        Response response = authenticate(username, password);
        String token = response.getStatusCode() == 200 || response.getStatusCode() == 201
                ? response.jsonPath().getString(ConfigReader.getProperty("api.auth.token.path", "token"))
                : null;
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Could not obtain an API token: HTTP " + response.getStatusCode()
                    + " " + response.getBody().asString());
        }
        return token;
    }

    /**
     * Creates an employee with POST. Pass a null token to send the request unauthenticated.
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
     * Retrieves an employee by ID using GET.
     */
    public static Response getEmployeeById(String employeeId, String token) {
        return RestAssured.given()
                .spec(ApiSpecFactory.getAuthenticatedRequestSpec(token))
                .pathParam("id", employeeId)
                .when()
                .get(getEmployeesEndpoint() + "/{id}")
                .then()
                .extract()
                .response();
    }

    /**
     * Extracts the created employee's id using api.employee.id.path.
     */
    public static String extractEmployeeId(Response response) {
        return response.jsonPath().getString(ConfigReader.getProperty("api.employee.id.path", "id"));
    }
}
