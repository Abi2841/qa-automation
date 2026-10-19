package com.uplift.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uplift.data.TestDataGenerator;

import java.time.LocalDate;

/**
 * Data model representing an individual Employee.
 * Fields follow the "Add employee" form: name, email, mobile, date of birth, PPSN and start date.
 * Also serialized as the REST request body (null fields are omitted); dates are ISO yyyy-MM-dd.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeModel {

    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String dateOfBirth;   // must make the employee at least 18
    private String ppsn;          // 7 digits + 1-2 letters, e.g. 1234567T
    private String startDate;
    private String employerId;    // only sent to the API when api.employer.id is configured

    public EmployeeModel() {
    }

    /**
     * Copy constructor so a scenario can change one field without touching the original.
     */
    public EmployeeModel(EmployeeModel other) {
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.email = other.email;
        this.mobileNumber = other.mobileNumber;
        this.dateOfBirth = other.dateOfBirth;
        this.ppsn = other.ppsn;
        this.startDate = other.startDate;
        this.employerId = other.employerId;
    }

    /**
     * Generates a valid Employee record with unique values and QA-AUTO- prefix:
     * checksum-valid PPSN, Irish mobile, 30-year-old date of birth, start date today.
     */
    public static EmployeeModel generateRandomEmployee() {
        EmployeeModel employee = new EmployeeModel();
        employee.setFirstName(TestDataGenerator.prefixed("Employee"));
        employee.setLastName("Member" + TestDataGenerator.uniqueLetters());
        employee.setEmail(TestDataGenerator.email("employee"));
        employee.setMobileNumber(TestDataGenerator.irishMobile());
        employee.setDateOfBirth(LocalDate.now().minusYears(30).toString());
        employee.setPpsn(TestDataGenerator.validPpsn());
        employee.setStartDate(LocalDate.now().toString());
        return employee;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPpsn() {
        return ppsn;
    }

    public void setPpsn(String ppsn) {
        this.ppsn = ppsn;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName() + " <" + email + ">, PPSN " + ppsn + ", mobile " + mobileNumber
                + ", DOB " + dateOfBirth + ", start " + startDate;
    }
}
