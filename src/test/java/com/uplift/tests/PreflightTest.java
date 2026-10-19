package com.uplift.tests;

import com.uplift.api.EmployeeApiService;
import com.uplift.base.DriverFactory;
import com.uplift.config.ConfigReader;
import com.uplift.reporting.ExtentTestListener;
import com.uplift.reporting.ReportLogger;
import com.uplift.utils.EmailInboxHandler;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Preflight check: run this first after filling in the QA URLs and credentials.
 * It tells you in seconds what is missing or wrong (values not set, URL unreachable,
 * API login rejected, inbox login failed) before the long UI journey starts.
 * Passwords and tokens are never printed.
 */
@Listeners(ExtentTestListener.class)
public class PreflightTest {

    private static final String[][] REQUIRED = {
            {"admin.portal.url", "environments/qa.properties or -Dadmin.portal.url"},
            {"employer.portal.url", "environments/qa.properties or -Demployer.portal.url"},
            {"api.base.url", "environments/qa.properties or -Dapi.base.url"},
            {"api.auth.endpoint", "environments/qa.properties (path from Swagger, e.g. /auth/login)"},
            {"api.employees.endpoint", "environments/qa.properties (path from Swagger, e.g. /employees)"},
            {"admin.username", "secrets.properties or ADMIN_USERNAME"},
            {"admin.password", "secrets.properties or ADMIN_PASSWORD"},
            {"api.auth.username", "secrets.properties or API_AUTH_USERNAME"},
            {"api.auth.password", "secrets.properties or API_AUTH_PASSWORD"},
            {"inbox.address", "secrets.properties (Gmail address used for +alias emails)"},
            {"inbox.username", "secrets.properties or INBOX_USERNAME"},
            {"inbox.password", "secrets.properties or INBOX_PASSWORD (Gmail App Password)"}
    };

    @Test(priority = 1, description = "Preflight 1: every required URL, endpoint and credential is set")
    public void requiredValuesAreSet() {
        List<String> missing = new ArrayList<>();
        for (String[] item : REQUIRED) {
            if (ConfigReader.isSet(item[0])) {
                ReportLogger.info("OK       " + item[0]);
            } else {
                missing.add(item[0] + "   -> set in " + item[1]);
            }
        }
        ReportLogger.info("Active environment: " + ConfigReader.getEnvironment());
        Assert.assertTrue(missing.isEmpty(), "Missing values:\n  " + String.join("\n  ", missing));
    }

    @Test(priority = 2, description = "Preflight 2: Admin portal URL is reachable")
    public void adminPortalIsReachable() {
        assertReachable("admin.portal.url");
    }

    @Test(priority = 3, description = "Preflight 3: Employer portal URL is reachable")
    public void employerPortalIsReachable() {
        assertReachable("employer.portal.url");
    }

    @Test(priority = 4, description = "Preflight 4: API base URL is reachable")
    public void apiBaseIsReachable() {
        assertReachable("api.base.url");
    }

    @Test(priority = 5, description = "Preflight 5: the browser starts and opens the Admin portal")
    public void browserOpensAdminPortal() {
        skipUnlessSet("admin.portal.url");
        WebDriver driver = DriverFactory.initDriver();
        try {
            driver.get(ConfigReader.getRequired("admin.portal.url"));
            ReportLogger.info("Browser opened: '" + driver.getTitle() + "' at " + driver.getCurrentUrl());
            Assert.assertFalse(driver.getCurrentUrl().startsWith("chrome-error") || driver.getCurrentUrl().startsWith("data:"),
                    "The Admin portal page did not load in the browser");
        } finally {
            DriverFactory.quitDriver();
        }
    }

    @Test(priority = 6, description = "Preflight 6: API login returns a token with the documented credentials")
    public void apiLoginReturnsToken() {
        skipUnlessSet("api.base.url", "api.auth.endpoint", "api.auth.username", "api.auth.password");
        try {
            String token = EmployeeApiService.getToken(
                    ConfigReader.getRequired("api.auth.username"), ConfigReader.getRequired("api.auth.password"));
            ReportLogger.info("Token received (" + token.length() + " characters)");
        } catch (IllegalStateException e) {
            Assert.fail(e.getMessage() + "\nCheck api.auth.endpoint, api.auth.username.field, "
                    + "api.auth.password.field and api.auth.token.path against the Swagger.");
        }
    }

    @Test(priority = 7, description = "Preflight 7: test inbox login (IMAP) works")
    public void inboxLoginWorks() {
        skipUnlessSet("inbox.username", "inbox.password");
        try {
            EmailInboxHandler.verifyLogin();
            ReportLogger.info("Inbox login OK for " + ConfigReader.getProperty("inbox.username"));
        } catch (Exception e) {
            Assert.fail("Cannot log in to the inbox: " + e.getMessage()
                    + "\nGmail needs 2-step verification and an App Password (not the normal password).");
        }
    }

    private static void skipUnlessSet(String... keys) {
        for (String key : keys) {
            if (!ConfigReader.isSet(key)) {
                throw new SkipException("'" + key + "' is not set yet");
            }
        }
    }

    private static void assertReachable(String key) {
        skipUnlessSet(key);
        String url = ConfigReader.getRequired(key);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<Void> response = client.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build(),
                    HttpResponse.BodyHandlers.discarding());
            ReportLogger.info(url + " -> HTTP " + response.statusCode());
            // 401/403 still means the server answered; only server errors are a problem
            Assert.assertTrue(response.statusCode() < 500, url + " answered with HTTP " + response.statusCode());
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            Assert.fail("Cannot reach " + url + " (" + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "). Check the URL and your network/VPN.");
        }
    }
}
