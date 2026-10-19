# Workplace Pension Platform (Ireland) - QA Automation Framework

Java 17 · Selenium WebDriver 4 · TestNG · REST Assured · ExtentReports 5 · Maven · Jenkins

Done by Arun

Automates the journey **Admin creates employer → Admin adds employer admin → Employer admin adds employees → Employees appear in the list and receive an invite**, across the Admin portal, the Employer portal and the API.

---

## 1. Status at a glance

| Part | Requirement | Implementation | Verified |
| :--- | :--- | :--- | :--- |
| A | 15-25 test cases + automate/manual reasoning | `docs/TEST_CASES_PART_A.md` (24 cases, 17 automated) | ✅ |
| B | Admin login → employer (name, CRN, address lookup, payroll date, IBAN/BIC) → employer admin (name, email, mobile, economic status, occupation, PEP) | `AdminPortalJourneyTest` | ⏳ needs QA env |
| B | Invite email → verification code → set password → dashboard for that employer only → login | `EmployerPortalJourneyTest` + `EmailInboxHandler` (IMAP) | ⏳ needs QA env + inbox |
| B | Manual employee, bulk upload from portal template (3 valid / 2 invalid, reason per row), logout | `EmployerPortalJourneyTest` | ⏳ needs QA env |
| C | ONE data-driven test: empty fields, email, PPSN, mobile, duplicate, under 18 | `EmployeeValidationTest` + `testdata/employee-validation-scenarios.json` (16 rows) | ⏳ needs QA env |
| D | Token, 401 without token, 201 + schema, GET read-back, 400 bad PPSN, UI↔API | `EmployeeApiTest` + `schemas/employee-response.schema.json` | ⏳ needs QA API + Swagger |
| E | Jenkinsfile: 5 stages, parameters, credentials store, HTML report, artifacts, unstable on failure, nightly cron | `Jenkinsfile` | ⏳ needs a Jenkins run |
| F | Build on push (pollSCM), failure email, employee invite email check | `Jenkinsfile`, `employeeReceivesIntroductionEmail` | ⏳ |
| — | Framework self-test (config, generators, CSV builder, browser, HTTP client, report, failure screenshots) | `testng-smoke.xml` on `-Denv=demo` | ✅ 10/10 passing |

> **Honest disclosure.** The QA URLs, Admin login and Swagger were not available while this framework was built, so the journey, validation and API suites have **not yet been executed against the QA environment**. Locators are anchored on the field labels named in the assignment's application guide (`Add employer`, `IBAN`, `PPSN`, …) and are kept in the page objects only. The first QA run may need small locator adjustments in those classes. Without configuration, every suite fails within seconds with a message naming the missing value, instead of timing out.

---

## 2. Architecture

```
src/main/java/com/uplift
├── config/ConfigReader         layered config: -D > env var > secrets.properties > environments/<env>.properties > config.properties
├── base/DriverFactory          ThreadLocal WebDriver (chrome|edge|firefox, headless, download folder)
├── base/BaseTest, BaseApiTest  one browser per test class / REST Assured base spec
├── pages/                      Page Object Model - the ONLY place with locators
│   ├── BasePage, PortalPage    label-based locators, explicit waits, dropdown/date/radio helpers, logout
│   ├── admin/                  AdminLoginPage, AdminDashboardPage, EmployersPage, CreateEmployerPage, EmployerDetailsPage
│   └── employer/               EmployerLoginPage, EmployerActivationPage, EmployerDashboardPage, EmployeesPage,
│                               AddEmployeePage, BulkUploadPage
├── api/                        ApiSpecFactory, ApiClient, EmployeeApiService (reusable token + employee helpers)
├── models/                     EmployerModel, EmployerAdminModel, EmployeeModel
├── data/                       TestDataGenerator (unique QA-AUTO- data, valid PPSN check letter, mod-97 IBAN),
│                               CsvTestDataGenerator (CSV from the portal template), JourneyContext, TestDataManager
├── reporting/                  ExtentManager, ExtentTestListener (screenshot on failure), ReportLogger
└── utils/                      WaitUtils (no Thread.sleep), ScreenshotUtils, EmailInboxHandler (IMAP test inbox)

src/test/java/com/uplift/tests
├── ui/AdminPortalJourneyTest       Part B steps 1-3
├── ui/EmployerPortalJourneyTest    Part B steps 4-7 (+ Part F invite email)
├── ui/EmployeeValidationTest       Part C (single @Test, DataProvider)
├── api/EmployeeApiTest             Part D
└── FrameworkSmokeTest, api/SmokeApiTest, ui/SmokeUiTest   framework self-test

src/test/resources
├── config.properties, environments/qa.properties, environments/demo.properties, secrets.properties.example
├── testng-regression.xml (default), testng-ui.xml, testng-api.xml, testng-validation.xml, testng-smoke.xml
├── testdata/employee-validation-scenarios.json
└── schemas/employee-response.schema.json
```

How a run flows: Maven Surefire starts the TestNG suite → UI tests drive page objects with explicit waits → API tests call `EmployeeApiService` → the journey stores what it created in `JourneyContext`, so the Employer portal logs in as **the admin just created**, and Part C and the API cross-check reuse that admin → `ExtentTestListener` writes `reports/ExtentReport.html`, embedding a screenshot and saving it to `reports/screenshots/` on failure.

Key design decisions:
- **Locators by visible label.** The guide names the fields, and labels change less often than generated ids.
- **One browser per class with one `@Test` per journey step.** The report shows exactly which step failed, and later steps are *skipped*, not failed.
- **Unique data per run.** Every entity is prefixed `QA-AUTO-` and carries a run id. PPSNs carry a valid mod-23 check letter and IBANs have valid mod-97 check digits, so "valid" data is genuinely valid.
- **Template-driven CSV.** The bulk file is built from the template downloaded in the portal, so the columns always match. Each invalid row has exactly one fault (invalid email, invalid PPSN).
- **No `Thread.sleep`.** Explicit waits throughout, the implicit wait is 0, and inbox and download polling use `FluentWait`.

---

## 3. Setup

Prerequisites: JDK 17+, Maven 3.9+, Chrome (or Edge/Firefox). Selenium Manager downloads the driver automatically.

```powershell
java -version
mvn -v
mvn -B clean test-compile        # resolves dependencies and compiles
```

### Configure the QA environment (values are sent separately with the assignment)

1. **URLs and API contract.** Fill `src/test/resources/environments/qa.properties` (`admin.portal.url`, `employer.portal.url`, `api.base.url`, `api.auth.endpoint`, `api.employees.endpoint`). Alternatively, pass them as `-D…` flags or environment variables (`ADMIN_PORTAL_URL`, …). Align the field names and `schemas/employee-response.schema.json` with the Swagger.
2. **Secrets.** Copy `src/test/resources/secrets.properties.example` to `secrets.properties` (gitignored) and fill in the Admin login, the API credentials and the test inbox.
3. **Test inbox.** Use a Gmail account with 2-step verification and an [App Password](https://myaccount.google.com/apppasswords). The admin and employee emails become `<you>+qa-auto-…@gmail.com`, so each run's invite is found by its unique address.

Only the provider's QA environment is ever targeted. Every created record is prefixed `QA-AUTO-` for cleanup.

---

## 4. How to run

> PowerShell: quote every `-D` argument, e.g. `"-DsuiteXmlFile=…"`.

| Goal | Command |
| :--- | :--- |
| **Everything (one command)**: framework checks, Part B, Part C, Part D | `mvn clean test` |
| UI only (journey + data-driven checks) | `mvn test "-DsuiteXmlFile=src/test/resources/testng-ui.xml"` |
| API only | `mvn test "-DsuiteXmlFile=src/test/resources/testng-api.xml"` |
| Part C only (needs `employer.admin.email/password`) | `mvn test "-DsuiteXmlFile=src/test/resources/testng-validation.xml"` |
| Framework self-test (no QA access needed) | `mvn test "-Denv=demo" "-DsuiteXmlFile=src/test/resources/testng-smoke.xml"` |
| Watch the browser / other browser | add `"-Dheadless=false"` and/or `"-Dbrowser=edge"` |
| Local copy of the Jenkins stages | `.\run-pipeline.ps1 -Environment qa -Browser chrome` |

Outputs:
- `reports/ExtentReport.html` (HTML report)
- `reports/screenshots/` (failure screenshots)
- `target/surefire-reports/` (TestNG/JUnit XML)
- `target/downloads/` (downloaded CSV template) and `target/test-data/` (generated CSV)

---

## 5. Jenkins (Part E)

`Jenkinsfile` stages: **Checkout → Install → API tests → UI tests → Publish report**.
- Parameters: `ENVIRONMENT` (qa/demo), `BROWSER`, `HEADLESS`, optional `NOTIFY_EMAIL`.
- Credentials come from the Jenkins store (`qa-admin-credentials`, `qa-api-credentials`, `qa-test-inbox`, `qa-employer-admin`). They are bound as environment variables, which `ConfigReader` reads, and never appear on the command line or in the repo.
- Each stage writes its own report (`reports/api`, `reports/ui`) and Surefire folder, so nothing is overwritten. `publishHTML` shows both on the build page. `archiveArtifacts` keeps the reports, screenshots and per-test logs.
- Test failures don't stop the pipeline (`-Dmaven.test.failure.ignore`). The `junit` step marks the build **UNSTABLE**, and a compile or pipeline error marks it **FAILED**.
- Triggers: `cron('H 2 * * *')` nightly and `pollSCM('H/5 * * * *')` on push (Part F). Failed or unstable builds email `NOTIFY_EMAIL`.

Local Jenkins: `docker run -p 8080:8080 jenkins/jenkins:lts`. The agent also needs Maven and a browser, so a Windows Jenkins (`jenkins.war`) on the same machine is the simplest option here.

---

## 6. Assumptions

1. Field labels on screen are close to the application guide. If they differ, only the page objects change.
2. The address lookup shows a suggestion list (`role="option"` or a list) and the first suggestion is acceptable. The query is set by `employer.address.lookup`.
3. Economic status and occupation are dropdowns. If the configured value isn't offered, the first real option is chosen and logged in the report.
4. The invite email contains the verification code next to the word "code" (`inbox.code.regex`) and a link to the Employer portal.
5. The upload summary shows counts in words such as "3 rows added / 2 rejected". Rejected rows show their email or row number plus a reason that names the field.
6. API request and response field names match the model (`firstName`, `ppsn`, …). The endpoints, token path and id path are configurable.
7. The API-created employee belongs to the employer of the employer admin used in the cross-check (`api.employer.id` can be sent if required).

---

## 7. Bugs / UX issues

No application bugs can be reported yet, because the QA environment has not been exercised (see the disclosure above). Issues found and fixed in this framework during review:
- **Assertions that could never fail.** API checks accepted 201 for "expect 401", and bulk counts were asserted as `>= 0`. All assertions are now strict.
- **Invalid "valid" data.** Random PPSN check letters were almost always wrong, which would fail checksum validation. UK sort code and account number were used where the guide requires IBAN/BIC.
- **Results lost in Jenkins.** The API and UI stages overwrote each other's report and Surefire XML, and test failures never produced an UNSTABLE build.
- **Credentials and placeholder secrets committed** in CI files, runner scripts and code defaults. All removed; secrets now come only from the credentials store, environment variables or the gitignored file.
- **XML suite error.** An unescaped `&` in `testng-validation.xml` (fixed earlier).

---

## 8. Answers to written questions

**1. If a test fails in Jenkins, how do you find out whether the problem is the application or the test?**
Start with the Extent report: the failure message, the screenshot and URL at the moment of failure, and the step that failed (each journey step is its own test). An assertion on application behaviour (wrong status code, missing row, an error banner in the screenshot) points to the application. A `TimeoutException` on a locator with the page looking correct points to the test (locator drift or timing). Then reproduce: rerun the single test headed against the same environment, and repeat the step by hand or through the API. If the API test for the same rule also fails, it's the application. If it passes manually and fails only in automation, fix the test.

**2. How would you run the UI tests in parallel on Jenkins agents and alert the team on failure?**
The driver is already `ThreadLocal`, and each test class gets its own browser, so TestNG `parallel="classes"` works for independent classes (the dependent journey stays in one class). Across agents: a declarative `parallel { }` block with one stage per browser (`-Dbrowser=chrome` / `edge`), or one per suite, each on a labelled agent or a Selenium Grid / Docker browser node. Alerting: `post { unstable/failure { … } }` already emails `NOTIFY_EMAIL`; Slack works the same way with `slackSend`, including the report link.

**3. You are the only tester. How would you split effort between UI, API and mobile?**
Roughly 50% API, 30% web UI, 20% mobile/exploratory. Business rules (PPSN, age, duplicates, auth) are cheapest and most stable to check at the API. UI automation covers the few end-to-end journeys that matter most (this one). Mobile gets a thin Appium smoke (launch, registration validation) plus exploratory sessions on real devices, because identity checks and app-store builds are hard to automate reliably.

**4. How would you test payment reconciliation, where a bank file must match an employer's contribution payment?**
Treat it as data-driven backend testing. Create a `QA-AUTO-` employer with a known contribution schedule, then generate bank files for each case: exact match, underpayment, overpayment, wrong or missing reference, duplicate file, and payment on the wrong date. Feed each through the same ingestion path production uses and assert the outcome per case: reconciled, partially paid with an exception raised, refund or hold, a duplicate rejected without double-allocating money, and an audit trail written. Also check amounts to the cent, currency, and that member-level allocations add up to the payment.

**5. How would you handle flaky tests caused by slow emails or third-party identity checks?**
Emails: never fixed sleeps. Poll the inbox with a timeout (this framework uses `FluentWait` over IMAP with a unique `+alias` per run, so stale emails can't match). Keep the email assertion in its own test step so a slow email doesn't hide other results. Long term, use a mail-catcher API or a test-only endpoint in QA. Third-party identity checks: stub them in QA (e.g. WireMock) with fixed "pass / fail / PEP match" personas, and keep a small number of contract tests against the real provider. Never use blanket retries, which hide real defects; use at most one retry, limited to known external steps and reported as such.

**6. How would you manage test data so the QA environment does not fill up with junk?**
Use a fixed prefix (`QA-AUTO-`) plus a run id on everything created, which this framework does. Clean up through the API at the end of a run where delete or archive endpoints exist, plus a scheduled job that removes `QA-AUTO-%` records older than 24-48 hours. Reuse long-lived reference data (one activated employer admin for API and Part C runs) instead of creating it every time. Agree a data-retention rule with the developers so cleanup never touches non-test data.

**7. What would you add next with another week?**
1. Run against the QA environment, tune the locators, and record a full green report plus Jenkins evidence.
2. API cleanup of `QA-AUTO-` data after each run.
3. Automate TC-05 (invalid IBAN) and TC-22 (bad upload file).
4. Cross-browser parallel stage in Jenkins.
5. The Appium registration smoke test.
6. Response-time thresholds on the API tests.

---

## 9. Submission checklist

- [ ] QA run: `mvn clean test`, then attach `reports/ExtentReport.html`
- [ ] Jenkins screenshots: stage view, console output, published report
- [ ] Screen recording (up to 5 minutes) of the suite running in Jenkins
- [ ] Bugs / UX issues found during the QA run added to section 7
