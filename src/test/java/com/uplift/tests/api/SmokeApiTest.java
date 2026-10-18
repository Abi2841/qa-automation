package com.uplift.tests.api;

import com.uplift.api.ApiClient;
import com.uplift.base.BaseApiTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * API smoke test verifying REST Assured configuration and HTTP client execution.
 */
public class SmokeApiTest extends BaseApiTest {

    @Test(description = "Verify GET request against QA API base endpoint returns HTTP 200")
    public void testApiGetEndpoint() {
        Response response = ApiClient.get("/users?page=1", requestSpec);

        Assert.assertEquals(response.getStatusCode(), 200, "HTTP Status should be 200 OK");
        Assert.assertNotNull(response.jsonPath().get("data"), "Response should contain data array");
        Assert.assertEquals(response.jsonPath().getInt("page"), 1, "Page number should be 1");
    }
}
