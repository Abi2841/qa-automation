package com.uplift.models;

import com.uplift.config.ConfigReader;
import com.uplift.data.TestDataGenerator;

/**
 * Data model representing an Employer organization.
 * Fields follow the Admin portal "Add employer" form: company name, CRN, address (lookup),
 * payroll date and bank details (IBAN, BIC).
 */
public class EmployerModel {

    private String companyName;
    private String companyRegistrationNumber; // CRN
    private String addressLookupQuery;        // typed into the address lookup; first suggestion is selected
    private String payrollDay;                // day of month payroll runs, e.g. "25"
    private String iban;
    private String bic;

    /**
     * Generates a unique, valid Employer dataset prefixed with QA-AUTO-.
     */
    public static EmployerModel generateRandomEmployer() {
        EmployerModel employer = new EmployerModel();
        employer.setCompanyName(TestDataGenerator.prefixed("Employer-" + TestDataGenerator.uniqueId()));
        employer.setCompanyRegistrationNumber(TestDataGenerator.companyRegistrationNumber());
        employer.setAddressLookupQuery(ConfigReader.getProperty("employer.address.lookup", "Dublin"));
        employer.setPayrollDay(ConfigReader.getProperty("employer.payroll.day", "25"));
        employer.setIban(TestDataGenerator.irishIban(ConfigReader.getProperty("employer.bank.code", "AIBK")));
        employer.setBic(ConfigReader.getProperty("employer.bic", "AIBKIE2D"));
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

    public String getAddressLookupQuery() {
        return addressLookupQuery;
    }

    public void setAddressLookupQuery(String addressLookupQuery) {
        this.addressLookupQuery = addressLookupQuery;
    }

    public String getPayrollDay() {
        return payrollDay;
    }

    public void setPayrollDay(String payrollDay) {
        this.payrollDay = payrollDay;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    @Override
    public String toString() {
        return companyName + " (CRN " + companyRegistrationNumber + ", IBAN " + iban + ", BIC " + bic
                + ", payroll day " + payrollDay + ")";
    }
}
