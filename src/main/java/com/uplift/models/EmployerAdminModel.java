package com.uplift.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Data model representing an Employer Administrator user.
 * Generates unique test data with the required 'QA-AUTO-' prefix.
 */
public class EmployerAdminModel {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;

    public EmployerAdminModel() {
    }

    public EmployerAdminModel(String firstName, String lastName, String email, String phoneNumber, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    /**
     * Generates a unique Employer Admin record prefixed with QA-AUTO-.
     */
    public static EmployerAdminModel generateRandomEmployerAdmin() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomDigits = 1000 + new Random().nextInt(9000);

        EmployerAdminModel admin = new EmployerAdminModel();
        admin.setFirstName("QA-AUTO-Admin");
        admin.setLastName("User" + randomDigits);
        admin.setEmail("qa-auto-admin-" + timestamp + "@testcompany.com");
        admin.setPhoneNumber("+44770090" + (100 + new Random().nextInt(899)));
        admin.setRole("Employer Administrator");
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
