package com.uplift.base;

import com.uplift.config.ConfigReader;
import com.uplift.reporting.ExtentTestListener;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Base test class for API automation test cases.
 * Configures REST Assured defaults, Base URI, and standard request specifications.
 */
@Listeners(ExtentTestListener.class)
public abstract class BaseApiTest {

    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setupApi() {
        RestAssured.baseURI = ConfigReader.getApiBaseUrl();

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        String authToken = ConfigReader.getSecret("api.auth.token", "");
        if (!authToken.isEmpty() && !authToken.startsWith("placeholder")) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        requestSpec = builder.build();
    }
}
