package com.uplift.data;

import com.uplift.config.ConfigReader;
import com.uplift.models.EmployeeModel;
import com.uplift.models.EmployerAdminModel;
import com.uplift.models.EmployerModel;

import java.time.Instant;

/**
 * Shares the data created by the end-to-end journey with later steps and suites in the same run
 * (the Employer portal logs in as the admin the Admin portal just created).
 *
 * When a suite runs on its own, the employer admin login falls back to the optional
 * employer.admin.email / employer.admin.password configuration.
 */
public final class JourneyContext {

    private static EmployerModel employer;
    private static EmployerAdminModel employerAdmin;
    private static String employerAdminPassword;
    private static EmployeeModel manualEmployee;
    private static Instant adminInviteSentAt;

    private JourneyContext() {
        // Prevent direct instantiation
    }

    public static Instant getAdminInviteSentAt() {
        return adminInviteSentAt;
    }

    public static void setAdminInviteSentAt(Instant value) {
        adminInviteSentAt = value;
    }

    public static EmployerModel getEmployer() {
        return employer;
    }

    public static void setEmployer(EmployerModel value) {
        employer = value;
    }

    public static EmployerAdminModel getEmployerAdmin() {
        return employerAdmin;
    }

    public static void setEmployerAdmin(EmployerAdminModel value) {
        employerAdmin = value;
    }

    public static void setEmployerAdminPassword(String value) {
        employerAdminPassword = value;
    }

    public static EmployeeModel getManualEmployee() {
        return manualEmployee;
    }

    public static void setManualEmployee(EmployeeModel value) {
        manualEmployee = value;
    }

    public static boolean hasActivatedEmployerAdmin() {
        return employerAdmin != null && employerAdminPassword != null;
    }

    public static String employerAdminEmail() {
        return hasActivatedEmployerAdmin() ? employerAdmin.getEmail() : ConfigReader.getRequired("employer.admin.email");
    }

    public static String employerAdminPassword() {
        return hasActivatedEmployerAdmin() ? employerAdminPassword : ConfigReader.getRequired("employer.admin.password");
    }
}
