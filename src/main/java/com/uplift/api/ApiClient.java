package com.uplift.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Reusable HTTP client wrapper built on REST Assured.
 */
public final class ApiClient {

    private ApiClient() {
        // Prevent direct instantiation
    }

    public static Response get(String endpoint, RequestSpecification spec) {
        return RestAssured.given()
                .spec(spec)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response post(String endpoint, Object body, RequestSpecification spec) {
        return RestAssured.given()
                .spec(spec)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response put(String endpoint, Object body, RequestSpecification spec) {
        return RestAssured.given()
                .spec(spec)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response delete(String endpoint, RequestSpecification spec) {
        return RestAssured.given()
                .spec(spec)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }
}
