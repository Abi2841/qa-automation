package com.uplift.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

/**
 * Utility to manage and load test fixtures and data files from classpath.
 */
public final class TestDataManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TestDataManager() {
        // Prevent direct instantiation
    }

    /**
     * Loads a JSON resource from the test classpath into a Jackson JsonNode.
     *
     * @param resourcePath relative path inside src/test/resources/ (e.g., "testdata/sample-data.json")
     * @return parsed JsonNode, or null if file not found
     */
    public static JsonNode getJsonNode(String resourcePath) {
        try (InputStream is = TestDataManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[WARN] Test data resource not found: " + resourcePath);
                return null;
            }
            return objectMapper.readTree(is);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to parse JSON test data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Maps a JSON resource directly to a typed Java class.
     */
    public static <T> T getDataAs(String resourcePath, Class<T> clazz) {
        try (InputStream is = TestDataManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return objectMapper.readValue(is, clazz);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to map JSON test data: " + e.getMessage());
            return null;
        }
    }
}
