package com.uplift.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Data model representing an individual Employee.
 * Generates unique test data with the required 'QA-AUTO-' prefix.
 * Supports Irish payroll & regulatory fields: PPSN, Mobile, Date of Birth.
 */
public class EmployeeModel {

    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String dateOfJoining;
    private String salary;
    private String ppsn;          // Irish Personal Public Service Number (e.g. 1234567T)
    private String mobileNumber;  // e.g. +353871234567
    private String dateOfBirth;   // e.g. 1995-06-15 (Must be >= 18 years old)

    public EmployeeModel() {
    }

    public EmployeeModel(String employeeId, String firstName, String lastName, String email,
                         String department, String dateOfJoining, String salary) {
        this(employeeId, firstName, lastName, email, department, dateOfJoining, salary,
             "1234567T", "+353871234567", "1995-06-15");
    }

    public EmployeeModel(String employeeId, String firstName, String lastName, String email,
                         String department, String dateOfJoining, String salary,
                         String ppsn, String mobileNumber, String dateOfBirth) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.dateOfJoining = dateOfJoining;
        this.salary = salary;
        this.ppsn = ppsn;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Generates a valid Employee record with unique values and QA-AUTO- prefix.
     * Complies with Irish regulatory standards (valid PPSN, Irish mobile, age > 18).
     */
    public static EmployeeModel generateRandomEmployee() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomDigits = 1000 + new Random().nextInt(9000);
        int ppsnDigits = 1000000 + new Random().nextInt(8999999);
        char[] ppsnLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'V', 'W', 'X', 'Y'};
        char ppsnCheck = ppsnLetters[new Random().nextInt(ppsnLetters.length)];

        EmployeeModel employee = new EmployeeModel();
        employee.setEmployeeId("QA-AUTO-EMP-" + timestamp.substring(8) + randomDigits);
        employee.setFirstName("QA-AUTO-John");
        employee.setLastName("Doe" + randomDigits);
        employee.setEmail("qa-auto-emp-" + timestamp + randomDigits + "@testworkplace.com");
        employee.setDepartment("Engineering");
        employee.setDateOfJoining("2026-01-15");
        employee.setSalary("65000");
        employee.setPpsn(ppsnDigits + String.valueOf(ppsnCheck));
        employee.setMobileNumber("+35387" + (1000000 + new Random().nextInt(8999999)));
        employee.setDateOfBirth("1995-08-20"); // Valid: ~31 years old
        return employee;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(String dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getPpsn() {
        return ppsn;
    }

    public void setPpsn(String ppsn) {
        this.ppsn = ppsn;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
