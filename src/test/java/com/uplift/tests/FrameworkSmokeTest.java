package com.uplift.tests;

import com.aventstack.extentreports.ExtentReports;
import com.fasterxml.jackson.databind.JsonNode;
import com.uplift.config.ConfigReader;
import com.uplift.data.CsvTestDataGenerator;
import com.uplift.data.TestDataGenerator;
import com.uplift.data.TestDataManager;
import com.uplift.models.EmployeeModel;
import com.uplift.models.EmployerModel;
import com.uplift.reporting.ExtentManager;
import com.uplift.reporting.ExtentTestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Framework verification smoke test: configuration, test data loading, data generators and reporting.
 * Needs no browser or network, so it runs in any environment.
 */
@Listeners(ExtentTestListener.class)
public class FrameworkSmokeTest {

    @Test(description = "Verify configuration reader loads common and environment properties")
    public void testConfigurationReaderLoadsDefaults() {
        Assert.assertNotNull(ConfigReader.getEnvironment(), "Environment should be resolved");
        Assert.assertNotNull(ConfigReader.getBrowser(), "Browser should not be null");
        Assert.assertTrue(ConfigReader.getExplicitWait() > 0, "Explicit wait should be greater than 0");
        Assert.assertEquals(ConfigReader.getTestDataPrefix(), "QA-AUTO-", "Test data prefix");
    }

    @Test(description = "Verify a missing required value fails fast with a message naming the key")
    public void testMissingRequiredConfigFailsFast() {
        IllegalStateException e = Assert.expectThrows(IllegalStateException.class,
                () -> ConfigReader.getRequired("qa.auto.key.that.does.not.exist"));
        Assert.assertTrue(e.getMessage().contains("qa.auto.key.that.does.not.exist"), e.getMessage());
    }

    @Test(description = "Verify test data manager loads JSON fixtures")
    public void testTestDataManagerLoadsJsonFixture() {
        JsonNode fixture = TestDataManager.getJsonNode("testdata/sample-data.json");
        Assert.assertNotNull(fixture, "Sample test data fixture should not be null");

        JsonNode userNode = fixture.get("smokeUser");
        Assert.assertNotNull(userNode, "smokeUser object should exist in sample-data.json");
        Assert.assertEquals(userNode.get("username").asText(), "test_qa_user");
    }

    @Test(description = "Verify generated PPSNs have a valid check letter and invalid ones are detected")
    public void testPpsnGenerator() {
        Assert.assertTrue(TestDataGenerator.isValidPpsn("1234567T"), "Known valid PPSN 1234567T");
        Assert.assertFalse(TestDataGenerator.isValidPpsn("1234567A"), "Wrong check letter");
        Assert.assertFalse(TestDataGenerator.isValidPpsn("12345AB"), "Too few digits");
        for (int i = 0; i < 200; i++) {
            String ppsn = TestDataGenerator.validPpsn();
            Assert.assertTrue(TestDataGenerator.isValidPpsn(ppsn), "Generated PPSN should be valid: " + ppsn);
        }
    }

    @Test(description = "Verify generated employer data is unique, QA-AUTO- prefixed and has a valid Irish IBAN")
    public void testEmployerAndEmployeeGenerators() {
        Assert.assertTrue(TestDataGenerator.isValidIban("IE29AIBK93115212345678"), "Published example IBAN");
        EmployerModel first = EmployerModel.generateRandomEmployer();
        EmployerModel second = EmployerModel.generateRandomEmployer();
        Assert.assertTrue(first.getCompanyName().startsWith("QA-AUTO-"), first.getCompanyName());
        Assert.assertNotEquals(first.getCompanyName(), second.getCompanyName(), "Employer names must be unique");
        Assert.assertTrue(first.getIban().matches("IE\\d{2}[A-Z]{4}\\d{14}"), first.getIban());
        Assert.assertTrue(TestDataGenerator.isValidIban(first.getIban()), "Generated IBAN checksum: " + first.getIban());

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        Assert.assertTrue(employee.getFirstName().startsWith("QA-AUTO-"), employee.getFirstName());
        Assert.assertTrue(employee.getMobileNumber().matches("\\+3538[3-9]\\d{7}"), employee.getMobileNumber());
        Assert.assertTrue(LocalDate.parse(employee.getDateOfBirth()).isBefore(LocalDate.now().minusYears(18)),
                "Generated employee must be an adult");
    }

    @Test(description = "Verify the bulk CSV follows the template's column order and maps each column")
    public void testCsvBuiltFromTemplateHeader() throws Exception {
        File template = new File("target/test-data/template-sample.csv");
        template.getParentFile().mkdirs();
        Files.write(template.toPath(), "﻿First Name,Last Name,Email,Mobile,Date of Birth,PPSN,Start Date\n"
                .getBytes(StandardCharsets.UTF_8));

        List<String> header = CsvTestDataGenerator.readTemplateHeader(template);
        Assert.assertEquals(header.get(0), "First Name", "BOM must be stripped from the first column");

        EmployeeModel employee = EmployeeModel.generateRandomEmployee();
        File csv = CsvTestDataGenerator.writeCsv(header, Arrays.asList(employee), "target/test-data/csv-sample.csv");
        List<String> lines = Files.readAllLines(csv.toPath());
        Assert.assertEquals(lines.size(), 2, "Header + one row");
        String[] values = lines.get(1).split(",");
        Assert.assertEquals(values[2], employee.getEmail(), "Email column");
        Assert.assertEquals(values[5], employee.getPpsn(), "PPSN column");
    }

    @Test(description = "Verify ExtentReports instance initializes with framework metadata")
    public void testExtentReportsInitialization() {
        ExtentReports reports = ExtentManager.getExtentReports();
        Assert.assertNotNull(reports, "ExtentReports instance should be initialized");
    }
}
