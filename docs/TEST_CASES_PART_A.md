# QA Automation Assignment (Round 2) - Part A: Test Case Specification

## 1. Executive Summary & Strategy

This document defines the **Part A** test case specification for the **Employer and Employee Onboarding Business Journey**. It balances functional coverage across Web UI and REST API interfaces, incorporating positive, negative, and edge scenarios.

### Automation Feasibility Criteria
- **Automated (High ROI):** Repetitive regression workflows, deterministic validation rules, multi-step end-to-end data pipelines (e.g., Admin → Employer → Employer Admin → Employee), data-driven bulk upload verification, and backend API contracts.
- **Manual (Exploratory / Low Automation ROI):** Visual aesthetics, file upload OS-dialog native interactions, ambiguous UX error messaging nuances, and exploratory boundary testing during initial feature release.

---

## 2. Test Case Specification Table (15–25 Scenarios)

| Test Case ID | Scenario | Test Type | Priority | Expected Result | Automated | Reason for Automating or Leaving Manual |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-AUTH-01** | Verify Super Admin logs into Admin Portal with valid credentials | Positive | High (P1) | Super Admin is authenticated and redirected to the Admin Dashboard; session token generated. | **Yes** | Critical gatekeeper for all Admin onboarding actions; essential regression test. |
| **TC-AUTH-02** | Verify Admin login fails with invalid credentials | Negative | High (P1) | Access is denied; error message displayed (e.g., "Invalid username or password"); user remains on login page. | **Yes** | Core security check; deterministic and fast to execute via automation. |
| **TC-AUTH-03** | Verify Employer Admin logs into Employer Portal with newly provisioned credentials | Positive | High (P1) | Employer Admin successfully logs in and is redirected to the Employer workforce dashboard. | **Yes** | Key handoff step in the business journey between Admin and Employer personas. |
| **TC-AUTH-04** | Verify user session terminates on logout | Positive | Medium (P2) | Session is invalidated; user is redirected to the login screen; backward browser navigation cannot access authenticated views. | **Yes** | Standard session security regression test; easily validated via automated cookies/session checks. |
| **TC-EMP-01** | Verify Admin creates a new employer with valid details (`QA-AUTO-` prefix) | Positive | High (P1) | Employer profile is saved; status shows "Active"; unique Employer ID is generated. | **Yes** | First primary step of the required business journey; automated to enable downstream flows. |
| **TC-EMP-02** | Verify Employer creation fails when mandatory fields are omitted | Negative | High (P1) | Form submission blocked; inline field validation errors highlight the missing mandatory fields. | **Yes** | Prevents bad data from entering the database; repetitive form validation ideal for automation. |
| **TC-EMP-03** | Verify Employer creation fails with a duplicate employer name / registration number | Negative | Medium (P2) | Submission is rejected with duplicate entity error (e.g., "Employer with this name already exists"). | **Yes** | Protects tenant uniqueness and database constraints; automated with dynamic test data. |
| **TC-EMP-04** | Verify Employer creation via backend REST API endpoint | Positive | High (P1) | HTTP 201 Created returned; response payload contains Employer ID and matches request data. | **Yes** | API automation provides fast, headless test data seeding and contract validation. |
| **TC-ADM-01** | Verify Admin adds an Employer Admin to an existing employer | Positive | High (P1) | Employer Admin user is created, linked to the employer, and assigned the Employer Admin role. | **Yes** | Step 2 of the business journey; enables Employer Admin login and subsequent employee management. |
| **TC-ADM-02** | Verify Employer Admin creation fails with invalid email format | Negative | Medium (P2) | Client-side/server-side validation displays error (e.g., "Please enter a valid email address"); record not saved. | **Yes** | Data-driven validation best automated across common invalid email permutations. |
| **TC-ADM-03** | Verify Employer Admin creation fails with duplicate email address | Negative | Medium (P2) | Error displayed indicating the email is already registered across the platform. | **Yes** | Crucial identity and authentication uniqueness rule; automated using data fixtures. |
| **TC-EE-01** | Verify Employer Admin adds an individual employee manually with valid details | Positive | High (P1) | Employee record is created; employee appears in the active employee directory table. | **Yes** | Step 4 of the business journey; verifies core manual creation UI form. |
| **TC-EE-02** | Verify manual employee creation fails when required fields are missing | Negative | High (P1) | Submission blocked; inline validation messages indicate mandatory fields cannot be blank. | **Yes** | Form-level validation; automated to verify required constraints across UI fields. |
| **TC-EE-03** | Verify manual employee creation rejects duplicate email / employee ID | Negative | Medium (P2) | Error notification shown (e.g., "Employee ID / Email already exists in this organization"). | **Yes** | Enforces organizational integrity rules; easily verified with automated assertions. |
| **TC-EE-04** | Verify date field boundaries for employee creation (e.g., Future DOB or invalid DOJ format) | Edge | Low (P3) | System rejects future Date of Birth and validates Date of Joining formatting rules. | **No** | **Manual:** Boundary date picker interactions and localized calendar widgets are prone to test flakiness and better suited for manual exploratory QA. |
| **TC-BLK-01** | Verify bulk employee CSV upload with 5 records (3 valid and 2 invalid records) | Edge / Positive | High (P1) | System processes the file partially: exactly 3 valid employees are imported; exactly 2 invalid records are rejected with row-specific errors. | **Yes** | **Core assignment requirement (Step 5)**; validates partial batch processing and error reporting logic. |
| **TC-BLK-02** | Verify bulk CSV upload rejects unsupported file formats (e.g., .pdf, .exe, .png) | Negative | Medium (P2) | File picker/upload rejects file; error message indicates "Only .csv or .xlsx files are supported". | **Yes** | Security and file validation check; automated by uploading non-CSV mime types. |
| **TC-BLK-03** | Verify bulk upload with an empty CSV file (headers only or 0 bytes) | Edge | Medium (P2) | Upload is rejected with error (e.g., "File is empty or contains no employee records"). | **Yes** | Prevents empty batch processing jobs from triggering backend workers. |
| **TC-BLK-04** | Verify bulk upload error report visual formatting and download experience | Edge | Low (P3) | Error report modal/download displays clear row numbers and error descriptions without visual overlap. | **No** | **Manual:** Visual layout, responsive styling, and download prompt UX are best evaluated manually. |
| **TC-API-01** | Verify GET employee directory API endpoint returns paginated results | Positive | High (P1) | HTTP 200 OK; response body contains paginated array matching created employees. | **Yes** | Fast backend verification ensuring UI and API state consistency. |
| **TC-API-02** | Verify API endpoints reject requests without a valid Bearer token | Negative | High (P1) | HTTP 401 Unauthorized returned; body contains error code indicating missing/expired token. | **Yes** | Critical security requirement; REST Assured executes this in milliseconds. |
| **TC-API-03** | Verify POST employee API fails with invalid JSON schema | Negative | Medium (P2) | HTTP 400 Bad Request returned with descriptive field validation error in JSON payload. | **Yes** | Ensures backend API schema integrity and prevents silent data corruption. |

---

## 3. Automation vs. Manual Rationale

### Why the Selected 19 Cases Are Automated:
1. **End-to-End Business Continuity:** The core 6-step flow (Admin login → Employer creation → Admin provisioning → Employer portal login → Manual employee → Bulk CSV upload → Verification → Logout) is a high-frequency regression path that takes significant time if done manually.
2. **Deterministic Outcomes:** Verification of HTTP status codes (200, 201, 400, 401), database record counts, and CSV batch processing (exactly 3 pass, 2 fail) have objective, machine-verifiable results.
3. **Execution Speed & CI/CD Readiness:** Automated API tests run in seconds and provide fast feedback in the Jenkins pipeline before heavy UI tests execute.

### Why Cases (TC-EE-04, TC-BLK-04) Are Better Suited for Manual Testing:
1. **Custom Calendar & Date-Picker Widgets:** Testing boundary dates across various browser locales often introduces brittle UI automation scripts that require constant maintenance.
2. **Visual & Usability Assessment:** Verifying whether error tables look clean, whether text wraps comfortably, and whether tooltips are intuitive requires human visual judgment.
