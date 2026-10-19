package com.uplift.data;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.DataProvider;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TestNG DataProvider supplying the Part C negative and boundary scenarios for the Add employee form.
 * All data lives in src/test/resources/testdata/employee-validation-scenarios.json - nothing is hard-coded here,
 * so a new validation rule is covered by adding a JSON entry, not code.
 */
public class EmployeeValidationDataProvider {

    private static final String DATA_FILE = "testdata/employee-validation-scenarios.json";

    /**
     * Each row: scenario id, description, field overrides (field name -> value), optional expected message text.
     */
    @DataProvider(name = "addEmployeeValidationScenarios")
    public static Object[][] addEmployeeValidationScenarios() {
        JsonNode root = TestDataManager.getJsonNode(DATA_FILE);
        if (root == null || !root.has("scenarios") || root.get("scenarios").size() == 0) {
            throw new IllegalStateException("No scenarios found in " + DATA_FILE);
        }
        JsonNode scenarios = root.get("scenarios");
        Object[][] data = new Object[scenarios.size()][4];
        for (int i = 0; i < scenarios.size(); i++) {
            JsonNode scenario = scenarios.get(i);
            Map<String, String> overrides = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = scenario.get("overrides").fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                overrides.put(field.getKey(), field.getValue().asText());
            }
            data[i][0] = scenario.get("id").asText();
            data[i][1] = scenario.get("description").asText();
            data[i][2] = overrides;
            data[i][3] = scenario.has("expectedMessageContains") ? scenario.get("expectedMessageContains").asText() : null;
        }
        return data;
    }
}
