package com.uplift.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Utility to generate fresh CSV bulk upload files dynamically for each test run.
 * Guarantees that valid rows have unique emails and IDs to avoid duplicate collisions,
 * while maintaining the 2 intentional validation flaws (Row 4 and Row 5).
 */
public final class CsvTestDataGenerator {

    private CsvTestDataGenerator() {
        // Prevent direct instantiation
    }

    /**
     * Generates a 5-record CSV file (3 valid, 2 invalid) at the target file location.
     */
    public static File createFiveRecordCsv(String targetFilePath) throws IOException {
        File file = new File(targetFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int rand = 1000 + new Random().nextInt(9000);

        String id1 = "QA-AUTO-E" + timestamp.substring(8) + "1";
        String id2 = "QA-AUTO-E" + timestamp.substring(8) + "2";
        String id3 = "QA-AUTO-E" + timestamp.substring(8) + "3";
        String id4 = "QA-AUTO-E" + timestamp.substring(8) + "4";

        try (FileWriter writer = new FileWriter(file)) {
            // Header row
            writer.write("Employee ID,First Name,Last Name,Email,Department,Date of Joining,Salary\n");

            // Row 1 - VALID
            writer.write(id1 + ",QA-AUTO-Alice,Smith,qa-auto-alice-" + timestamp + rand + "@example.com,Finance,2025-02-01,70000\n");

            // Row 2 - VALID
            writer.write(id2 + ",QA-AUTO-Bob,Jones,qa-auto-bob-" + timestamp + rand + "@example.com,Marketing,2025-03-15,55000\n");

            // Row 3 - VALID
            writer.write(id3 + ",QA-AUTO-Charlie,Brown,qa-auto-charlie-" + timestamp + rand + "@example.com,Operations,2025-04-10,60000\n");

            // Row 4 - INVALID (Malformed Email syntax)
            writer.write(id4 + ",QA-AUTO-Diana,Prince,invalid-email-missing-at-domain,Sales,2025-05-01,58000\n");

            // Row 5 - INVALID (Duplicate Employee ID of Row 1 & negative salary)
            writer.write(id1 + ",QA-AUTO-Edward,Taylor,qa-auto-edward-" + timestamp + rand + "@example.com,HR,2025-06-01,-1000\n");
        }

        return file;
    }
}
