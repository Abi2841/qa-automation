package com.uplift.api;

import com.uplift.config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Factory class for reusable REST Assured RequestSpecification and ResponseSpecification objects.
 * Encapsulates base URI, content type, authentication headers, and logging filters.
 */
public final class ApiSpecFactory {

    private ApiSpecFactory() {
        // Prevent direct instantiation
    }

    /**
     * Creates a standard unauthenticated JSON request specification.
     */
    public static RequestSpecification getDefaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .log(LogDetail.METHOD)
                .build();
    }

    /**
     * Creates an authenticated request specification injecting the Bearer token.
     */
    public static RequestSpecification getAuthenticatedRequestSpec(String token) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .log(LogDetail.METHOD);

        if (token != null && !token.trim().isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token.trim());
        }

        return builder.build();
    }

    /**
     * Creates a reusable response specification asserting an expected HTTP status code.
     */
    public static ResponseSpecification getResponseSpec(int expectedStatusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .expectContentType(ContentType.JSON)
                .build();
    }
}
