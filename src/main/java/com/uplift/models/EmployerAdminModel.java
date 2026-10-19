package com.uplift.models;

import com.uplift.config.ConfigReader;
import com.uplift.data.TestDataGenerator;

/**
 * Data model representing an Employer Administrator user.
 * Fields follow the "add an admin" form: name, email, mobile, economic status, occupation and the PEP question.
 * The email is a "+alias" of the configured test inbox so the invite and verification code can be read.
 */
public class EmployerAdminModel {

    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String economicStatus;
    private String occupation;
    private String pepAnswer; // "Yes" / "No"

    /**
     * Generates a unique Employer Admin record prefixed with QA-AUTO-.
     */
    public static EmployerAdminModel generateRandomEmployerAdmin() {
        EmployerAdminModel admin = new EmployerAdminModel();
        admin.setFirstName(TestDataGenerator.prefixed("Admin"));
        admin.setLastName("Tester" + TestDataGenerator.uniqueLetters());
        admin.setEmail(TestDataGenerator.email("admin"));
        admin.setMobileNumber(TestDataGenerator.irishMobile());
        admin.setEconomicStatus(ConfigReader.getProperty("employer.admin.economic.status", "Employed"));
        admin.setOccupation(ConfigReader.getProperty("employer.admin.occupation", "Manager"));
        admin.setPepAnswer(ConfigReader.getProperty("employer.admin.pep.answer", "No"));
        return admin;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEconomicStatus() {
        return economicStatus;
    }

    public void setEconomicStatus(String economicStatus) {
        this.economicStatus = economicStatus;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPepAnswer() {
        return pepAnswer;
    }

    public void setPepAnswer(String pepAnswer) {
        this.pepAnswer = pepAnswer;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
