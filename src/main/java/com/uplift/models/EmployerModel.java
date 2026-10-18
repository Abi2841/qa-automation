package com.uplift.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Data model representing an Employer organization.
 * Generates unique test data with the required 'QA-AUTO-' prefix.
 */
public class EmployerModel {

    private String companyName;
    private String companyRegistrationNumber; // CRN
    private String addressLine1;
    private String city;
    private String postalCode;
    private String country;
    private String payrollDate; // e.g. "25"
    private String bankName;
    private String accountNumber;
    private String sortCode;

    public EmployerModel() {
    }

    public EmployerModel(String companyName, String companyRegistrationNumber, String addressLine1,
                         String city, String postalCode, String country, String payrollDate,
                         String bankName, String accountNumber, String sortCode) {
        this.companyName = companyName;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.payrollDate = payrollDate;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    /**
     * Generates a unique, valid Employer dataset prefixed with QA-AUTO-.
     */
    public static EmployerModel generateRandomEmployer() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomDigits = 1000 + new Random().nextInt(9000);

        EmployerModel employer = new EmployerModel();
        employer.setCompanyName("QA-AUTO-Corp-" + timestamp);
        employer.setCompanyRegistrationNumber("CRN" + timestamp.substring(4) + randomDigits);
        employer.setAddressLine1("Suite " + randomDigits + ", Innovation Way");
        employer.setCity("London");
        employer.setPostalCode("EC1A 1BB");
        employer.setCountry("United Kingdom");
        employer.setPayrollDate("25");
        employer.setBankName("Barclays Commercial Bank");
        employer.setAccountNumber("20" + randomDigits + "891");
        employer.setSortCode("20-00-00");
        return employer;
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public void setCompanyRegistrationNumber(String companyRegistrationNumber) {
        this.companyRegistrationNumber = companyRegistrationNumber;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPayrollDate() {
        return payrollDate;
    }

    public void setPayrollDate(String payrollDate) {
        this.payrollDate = payrollDate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }
}
