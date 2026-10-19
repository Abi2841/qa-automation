package com.uplift.base;

import com.uplift.api.ApiSpecFactory;
import com.uplift.reporting.ExtentTestListener;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Base test class for API automation test cases.
 * Builds the standard request specification (base URI, JSON content type) from configuration.
 */
@Listeners(ExtentTestListener.class)
public abstract class BaseApiTest {

    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setupApi() {
        requestSpec = ApiSpecFactory.getDefaultRequestSpec();
    }
}
