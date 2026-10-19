package com.uplift.data;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployeeModel;
import com.uplift.reporting.ReportLogger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builds the bulk-upload CSV from the template downloaded from the Employer portal, so the file always
 * has exactly the columns (names and order) the portal expects.
 *
 * Each template column is mapped to an employee field by keyword (e.g. "PPSN", "Date of birth");
 * unmapped columns are left blank and reported as a warning.
 */
public final class CsvTestDataGenerator {

    private CsvTestDataGenerator() {
        // Prevent direct instantiation
    }

    /**
     * Reads the header row of the portal template.
     */
    public static List<String> readTemplateHeader(File template) throws IOException {
        List<String> lines = Files.readAllLines(template.toPath(), StandardCharsets.UTF_8);
        if (lines.isEmpty() || lines.get(0).trim().isEmpty()) {
            throw new IllegalStateException("Downloaded CSV template " + template + " has no header row");
        }
        String header = lines.get(0).replace("﻿", ""); // strip UTF-8 BOM written by Excel
        List<String> columns = new ArrayList<>();
        for (String column : header.split(",", -1)) {
            columns.add(column.trim().replaceAll("^\"|\"$", ""));
        }
        return columns;
    }

    /**
     * Writes one row per employee in the template's column order.
     */
    public static File writeCsv(List<String> header, List<EmployeeModel> rows, String targetFilePath) throws IOException {
        File file = new File(targetFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        List<Function<EmployeeModel, String>> mappers = new ArrayList<>();
        List<String> unmapped = new ArrayList<>();
        for (String column : header) {
            Function<EmployeeModel, String> mapper = mapperFor(column);
            if (mapper == null) {
                unmapped.add(column);
                mapper = e -> "";
            }
            mappers.add(mapper);
        }
        if (!unmapped.isEmpty()) {
            ReportLogger.warning("Template columns without test data (left blank): " + unmapped);
        }

        StringBuilder csv = new StringBuilder(String.join(",", header)).append("\n");
        for (EmployeeModel employee : rows) {
            List<String> values = new ArrayList<>();
            for (Function<EmployeeModel, String> mapper : mappers) {
                values.add(escape(mapper.apply(employee)));
            }
            csv.append(String.join(",", values)).append("\n");
        }
        Files.write(file.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static Function<EmployeeModel, String> mapperFor(String column) {
        String c = column.toLowerCase().replaceAll("[^a-z]", "");
        if (c.contains("first") || c.equals("forename") || c.equals("givenname")) {
            return EmployeeModel::getFirstName;
        }
        if (c.contains("last") || c.contains("surname") || c.contains("familyname")) {
            return EmployeeModel::getLastName;
        }
        if (c.equals("name") || c.equals("fullname") || c.equals("employeename")) {
            return EmployeeModel::getFullName;
        }
        if (c.contains("email")) {
            return EmployeeModel::getEmail;
        }
        if (c.contains("mobile") || c.contains("phone")) {
            return EmployeeModel::getMobileNumber;
        }
        if (c.contains("birth") || c.equals("dob")) {
            return e -> formatDate(e.getDateOfBirth());
        }
        if (c.contains("pps")) {
            return EmployeeModel::getPpsn;
        }
        if (c.contains("start") || c.contains("joining") || c.contains("employmentdate")) {
            return e -> formatDate(e.getStartDate());
        }
        return null;
    }

    private static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        return LocalDate.parse(isoDate).format(
                DateTimeFormatter.ofPattern(ConfigReader.getProperty("csv.date.format", "dd/MM/yyyy")));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
