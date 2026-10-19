# QA Automation Assignment (Round 2) - Part A: Test Case Specification

Journey under test: **Admin creates employer → Admin adds employer admin → Employer admin adds employees → Employees appear in the list and receive an invite.**

## 1. Test cases (24)

| ID | Scenario | Type | Priority | Automated | Where |
| :--- | :--- | :--- | :--- | :--- | :--- |
| TC-01 | Admin logs in to the Admin portal with the test account and lands on the dashboard | Positive | P1 | Yes | `AdminPortalJourneyTest.adminLogsIn` |
| TC-02 | Admin login with a wrong password is refused and stays on the login page | Negative | P1 | No | Manual (see 2) |
| TC-03 | Create an employer with unique `QA-AUTO-` data: name, CRN, address via lookup, payroll date, IBAN, BIC → success message, employer listed with its status | Positive | P1 | Yes | `AdminPortalJourneyTest.adminCreatesEmployer` |
| TC-04 | Create employer with mandatory fields empty → blocked with field errors | Negative | P2 | No | Manual |
| TC-05 | Create employer with an invalid IBAN (bad check digits) or BIC → rejected | Negative | P1 | No | Manual / next candidate |
| TC-06 | Create employer with a CRN that already exists → duplicate rejected | Negative | P2 | No | Manual |
| TC-07 | Add an employer admin (name, email, mobile, economic status, occupation, PEP = No) → admin listed | Positive | P1 | Yes | `AdminPortalJourneyTest.adminAddsEmployerAdmin` |
| TC-08 | Employer admin with PEP = Yes → flagged / extra compliance step as designed | Edge | P2 | No | Manual (compliance rule needs clarification) |
| TC-09 | Employer admin receives the invite email with a verification code | Positive | P1 | Yes | `EmployerPortalJourneyTest.employerAdminActivatesAccountFromInvite` |
| TC-10 | First login: follow the invite link, enter the code, set a password → dashboard for that employer only | Positive | P1 | Yes | `EmployerPortalJourneyTest.employerAdminActivatesAccountFromInvite` |
| TC-11 | First login with a wrong or expired verification code → rejected | Negative | P2 | No | Manual |
| TC-12 | Employer admin logs in again with the new password | Positive | P1 | Yes | `EmployerPortalJourneyTest.employerAdminLogsIn` |
| TC-13 | Add one employee manually (name, email, mobile, DOB, PPSN, start date) → appears in the employee list | Positive | P1 | Yes | `EmployerPortalJourneyTest.addEmployeeManually` |
| TC-14 | Manually added employee receives the introduction email to download the app | Positive | P2 | Yes | `EmployerPortalJourneyTest.employeeReceivesIntroductionEmail` |
| TC-15 | Add employee with mandatory fields empty | Negative | P1 | Yes | `EmployeeValidationTest` (C01-C03) |
| TC-16 | Add employee with invalid email formats | Negative | P1 | Yes | `EmployeeValidationTest` (C04-C06) |
| TC-17 | Add employee with invalid PPSN (not 7 digits + 1-2 letters) | Negative | P1 | Yes | `EmployeeValidationTest` (C07-C10) |
| TC-18 | Add employee with invalid mobile number | Negative | P2 | Yes | `EmployeeValidationTest` (C11-C12) |
| TC-19 | Add a duplicate employee (same PPSN, or same email) | Negative | P1 | Yes | `EmployeeValidationTest` (C13-C14) |
| TC-20 | Add employee under 18, including the boundary "turns 18 tomorrow" | Edge | P1 | Yes | `EmployeeValidationTest` (C15-C16) |
| TC-21 | Bulk upload from the downloaded portal template: 5 rows (3 valid, 2 invalid) → summary shows 3 added, 2 rejected with a reason per row; valid employees listed, invalid not | Positive / Negative | P1 | Yes | `EmployerPortalJourneyTest.bulkUploadThreeValidTwoInvalid` |
| TC-22 | Bulk upload of a non-CSV file or a template with headers only → clear error, nothing imported | Negative | P2 | No | Manual / next candidate |
| TC-23 | Logout ends the session: the dashboard URL requires login again | Positive | P1 | Yes | `EmployerPortalJourneyTest.employerAdminLogsOut` |
| TC-24 | API: token required (401), create employee (201 + schema), read back, bad PPSN (400 with message), API-created employee visible in the Employer portal | Positive / Negative | P1 | Yes | `EmployeeApiTest` (D1-D6) |

**17 automated, 7 manual.** The data-driven test (TC-15 to TC-20) runs 16 scenarios from `src/test/resources/testdata/employee-validation-scenarios.json`.

## 2. Why automate these and leave the others manual

**Automated:**
- **The core journey (TC-01, 03, 07, 09-10, 12-14, 21, 23).** It is the money path of the product, and each step creates data the next step needs. It is slow and error-prone by hand (inbox, codes, a CSV), so it gives the highest return as a nightly regression.
- **Add employee field rules (TC-15 to TC-20).** These are deterministic rules on regulated data (PPSN, age). One parameterised test covers many rows cheaply, and adding a rule means adding a JSON row, not code.
- **API checks (TC-24).** They are fast, stable and run first in CI. They prove the backend enforces auth and validation independently of the UI.

**Manual (for now):**
- **Negative cases on the Admin side (TC-02, 04-06, 11).** These matter, but each adds a new set of locators and data for a lower-risk screen. TC-05 (IBAN) and TC-22 (bad upload file) are the next automation candidates.
- **TC-08 (PEP = Yes).** The expected behaviour is a compliance decision that isn't described in the brief, so it needs clarification before it can be asserted.
- **Visual and UX quality.** Wording and layout of error messages and email content are judged by a person during exploratory testing.
