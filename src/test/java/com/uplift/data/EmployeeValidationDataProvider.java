package com.uplift.data;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.DataProvider;

/**
 * TestNG DataProvider supplying negative and edge test data for Employee validation.
 * Loads externalized datasets from src/test/resources/testdata/employee-validation-scenarios.json.
 */
public class EmployeeValidationDataProvider {

    private static final String DATA_FILE = "testdata/employee-validation-scenarios.json";

    @DataProvider(name = "invalidEmailProvider")
    public static Object[][] getInvalidEmails() {
        JsonNode root = TestDataManager.getJsonNode(DATA_FILE);
        if (root != null && root.has("invalidEmails")) {
            JsonNode array = root.get("invalidEmails");
            Object[][] data = new Object[array.size()][2];
            for (int i = 0; i < array.size(); i++) {
                data[i][0] = array.get(i).get("value").asText();
                data[i][1] = array.get(i).get("scenario").asText();
            }
            return data;
        }

        // Fallback default
        return new Object[][]{
                {"invalid-email-missing-at-domain", "Missing @ and domain"},
                {"qa.user@", "Missing domain after @"},
                {"@example.com", "Missing username before @"}
        };
    }

    @DataProvider(name = "invalidPpsnProvider")
    public static Object[][] getInvalidPpsns() {
        JsonNode root = TestDataManager.getJsonNode(DATA_FILE);
        if (root != null && root.has("invalidPpsns")) {
            JsonNode array = root.get("invalidPpsns");
            Object[][] data = new Object[array.size()][2];
            for (int i = 0; i < array.size(); i++) {
                data[i][0] = array.get(i).get("value").asText();
                data[i][1] = array.get(i).get("scenario").asText();
            }
            return data;
        }

        return new Object[][]{
                {"12345", "Too short (less than 7 digits)"},
                {"123456789", "Missing trailing check letter"},
                {"ABC1234T", "Alphabetic characters in numeric prefix"}
        };
    }

    @DataProvider(name = "invalidMobileProvider")
    public static Object[][] getInvalidMobiles() {
        JsonNode root = TestDataManager.getJsonNode(DATA_FILE);
        if (root != null && root.has("invalidMobiles")) {
            JsonNode array = root.get("invalidMobiles");
            Object[][] data = new Object[array.size()][2];
            for (int i = 0; i < array.size(); i++) {
                data[i][0] = array.get(i).get("value").asText();
                data[i][1] = array.get(i).get("scenario").asText();
            }
            return data;
        }

        return new Object[][]{
                {"12345", "Too short for Irish mobile"},
                {"+35387ABCDEF", "Alphabetic characters in mobile number"}
        };
    }

    @DataProvider(name = "under18DobProvider")
    public static Object[][] getUnder18Dobs() {
        JsonNode root = TestDataManager.getJsonNode(DATA_FILE);
        if (root != null && root.has("under18Dobs")) {
            JsonNode array = root.get("under18Dobs");
            Object[][] data = new Object[array.size()][3];
            for (int i = 0; i < array.size(); i++) {
                data[i][0] = array.get(i).get("value").asText();
                data[i][1] = array.get(i).get("age").asInt();
                data[i][2] = array.get(i).get("scenario").asText();
            }
            return data;
        }

        return new Object[][]{
                {"2012-05-15", 14, "14-year-old minor"},
                {"2009-10-01", 17, "17-year-old minor below 18-year minimum"}
        };
    }
}
