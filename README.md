# Workplace Pension Platform (Ireland) - QA Automation Framework

Java 17 · Selenium · TestNG · REST Assured · ExtentReports · Maven · Jenkins

**Done by Arun**

## 1. What this project does

It tests one journey of the pension platform automatically, through the web portals and the API:

**Admin creates an employer → Admin adds an employer admin → Employer admin adds employees → Employees appear in the list and receive an invite.**

```
ADMIN PORTAL                          EMPLOYER PORTAL                       API
log in                                read invite email (code + link)       get token / 401 without token
create employer (name, CRN,      ──►  set password, log in             ──►  create employee -> 201 + schema
 address, payroll date, IBAN, BIC)    add one employee                      read it back and compare
add employer admin (name, email,      bulk upload CSV: 3 valid, 2 invalid   bad PPSN -> 400 + message
 mobile, status, occupation, PEP)     16 invalid-data checks                employee visible in the portal
        │                             log out
        └── invite email ──► test inbox (Gmail)

Every test ──► HTML report, with a screenshot when a test fails
```

| Test | What it covers |
| :--- | :--- |
| `PreflightTest` | Checks your settings work before the long run: values set, URLs reachable, browser opens, API login, inbox login |
| `AdminPortalJourneyTest` | Admin login, create employer, add employer admin |
| `EmployerPortalJourneyTest` | Invite email, first login, add employee, bulk upload (3 valid + 2 invalid), intro email, logout |
| `EmployeeValidationTest` | One data-driven test: 16 invalid-data scenarios on the Add employee form (data in JSON) |
| `EmployeeApiTest` | Token, 401, create (201 + schema), read back, bad PPSN (400), portal cross-check |
| `FrameworkSmokeTest` | Offline self-test of config, data generators, CSV builder and report |

The 24 designed test cases (17 automated, 7 manual, with reasons) are in [docs/TEST_CASES_PART_A.md](docs/TEST_CASES_PART_A.md).

## 2. Current status

| Area | Status |
| :--- | :--- |
| Framework, test data, reporting, Jenkinsfile | Done |
| Self-test (`FrameworkSmokeTest`) | Passing 7 of 7 |
| Journey, validation and API tests | Written from the application guide. They need their first run against the QA environment (URLs and logins are supplied separately). Locators may need small adjustments in the page classes |
| Jenkins run and screenshots | To do after the first QA run |
| Optional extras | Appium mobile test and parallel browsers are not done |

## 3. Project structure

```
src/main/java/com/uplift
  pages/        Page Objects: the only place with locators (one class per screen)
  api/          API helper: token, create employee, get employee
  config/       ConfigReader: reads all settings
  data/         Unique QA-AUTO- test data, valid PPSN/IBAN generators, CSV builder
  models/       Employer, EmployerAdmin and Employee data classes
  utils/        Waits, screenshots, Gmail inbox reader
  reporting/    ExtentReports listener (report + failure screenshot)
  base/         Browser start/stop, base test classes
src/test/java/com/uplift/tests    The tests listed above
src/test/resources
  environments/qa.properties      URLs and API paths of the QA environment
  secrets.properties.example      Template for passwords (copy to secrets.properties)
  testng-*.xml                    Which tests run (preflight, ui, api, regression, smoke)
  testdata/                       Invalid-data scenarios (JSON)
  schemas/                        Expected API response (JSON schema)
Jenkinsfile                       Pipeline
docs/                             Test cases, extra notes
```

## 4. What you need

- **JDK 17 or newer** - check with `java -version`
- **Maven 3.9 or newer** - check with `mvn -v`
- **Google Chrome** (Edge or Firefox also work). The browser driver downloads automatically.
- **A Gmail account for test emails** with 2-step verification turned on and an [App Password](https://myaccount.google.com/apppasswords) created. Invite emails are sent to addresses like `yourname+qa-auto-...@gmail.com`, so each run finds only its own emails.
- **The QA details:** Admin portal URL, Employer portal URL, Admin login, API base URL and the Swagger documentation.

Only use the QA environment. Everything the tests create starts with `QA-AUTO-`, so it is easy to clean up.

## 5. Setup

**Step 1. Get the code and check the build**
```
git clone https://github.com/Aruniklesh/qa-automation.git
cd qa-automation
mvn -B clean test-compile
```
It should end with `BUILD SUCCESS`.

**Step 2. Fill in the URLs and API paths.** Open `src/test/resources/environments/qa.properties`:

| Key | What to enter |
| :--- | :--- |
| `admin.portal.url` | Admin portal address |
| `employer.portal.url` | Employer portal address |
| `api.base.url` | API base address |
| `api.auth.endpoint` | Login path from Swagger |
| `api.employees.endpoint` | Employees path from Swagger |
| `api.auth.username.field`, `api.auth.password.field` | Field names of the login request (default `email`, `password`) |
| `api.auth.token.path` | Where the token is in the login response (default `token`) |
| `api.employee.id.path` | Where the id is in the create response (default `id`) |
| `api.employer.id` | Only if the API needs an employer id |
| `employer.expected.status` | Optional: status text expected in the employer list, e.g. `Active` |

**Step 3. Add the passwords.** Copy `src/test/resources/secrets.properties.example` to `src/test/resources/secrets.properties` (this file is never uploaded to Git) and fill in:

| Key | What to enter |
| :--- | :--- |
| `admin.username`, `admin.password` | Admin portal login |
| `api.auth.username`, `api.auth.password` | API login from the Swagger documentation |
| `inbox.address` | The Gmail address, e.g. `yourname@gmail.com` |
| `inbox.username`, `inbox.password` | The same Gmail address and the **App Password** |

Any value can also be passed on the command line (`"-Dadmin.password=..."`) or as an environment variable (`ADMIN_PASSWORD`). This is how Jenkins supplies them.

**Step 4. Check the settings.**
```
mvn test "-DsuiteXmlFile=src/test/resources/testng-preflight.xml"
```
Each failure says what is wrong and where to fix it. Continue when all checks pass.

## 6. How to run

On Windows PowerShell, keep the quotes around every `-D` option.

| Goal | Command |
| :--- | :--- |
| **Run everything (one command)** | `mvn clean test` |
| Check settings first | `mvn test "-DsuiteXmlFile=src/test/resources/testng-preflight.xml"` |
| UI journey + validation checks | `mvn test "-DsuiteXmlFile=src/test/resources/testng-ui.xml"` |
| API tests only | `mvn test "-DsuiteXmlFile=src/test/resources/testng-api.xml"` |
| One test class | `mvn test "-Dtest=AdminPortalJourneyTest"` |
| Watch the browser | add `"-Dheadless=false"` |
| Use Edge or Firefox | add `"-Dbrowser=edge"` or `"-Dbrowser=firefox"` |
| Self-test without QA access | `mvn test "-DsuiteXmlFile=src/test/resources/testng-smoke.xml"` |

The validation tests and the API portal check need an employer admin. A full run creates one in the journey and reuses it. To run them alone, add `employer.admin.email` and `employer.admin.password` to `secrets.properties`.

## 7. Reports and results

| What | Where |
| :--- | :--- |
| HTML report (open in a browser) | `reports/ExtentReport.html` |
| Failure screenshots | `reports/screenshots/` |
| Raw test results | `target/surefire-reports/` |
| Generated bulk-upload CSV | `target/test-data/` |

In the report, green means passed, red means failed (with a screenshot and the reason), and grey means skipped because an earlier step failed.

## 8. Jenkins

The `Jenkinsfile` has the stages **Checkout → Install → API tests → UI tests → Publish report**.

**One-time Jenkins setup**
1. Install the plugins: Pipeline, Git, Credentials Binding, HTML Publisher, JUnit and Mailer. The Jenkins machine needs Java 17+, Maven and Chrome.
2. Add credentials (Manage Jenkins → Credentials, kind *Username with password*) with these exact IDs:

   | ID | Content |
   | :--- | :--- |
   | `qa-admin-credentials` | Admin portal login |
   | `qa-api-credentials` | API login |
   | `qa-test-inbox` | Gmail address and App Password |
   | `qa-employer-admin` | An already-activated employer admin (used by the API stage) |

3. Set `ADMIN_PORTAL_URL`, `EMPLOYER_PORTAL_URL` and `API_BASE_URL` as global environment variables (Manage Jenkins → System), or put them in `qa.properties`.
4. Create a **Pipeline** job: *Pipeline script from SCM* → Git → this repository → branch `*/main` → script path `Jenkinsfile`.

**Running it**
- Click **Build with Parameters**. Choose the browser and headless mode.
- It also runs nightly and checks the repository for new pushes about every 5 minutes (the first build must be started manually so Jenkins learns the triggers).
- Each stage keeps its own report. The HTML report is shown on the build page, and screenshots and logs are saved as build artifacts.
- If any test fails, the build is marked **UNSTABLE**. A build or pipeline error marks it **FAILED**.
- To get an email on failure, enter an address in the `NOTIFY_EMAIL` parameter.

## 9. If something goes wrong

| Message | Meaning and fix |
| :--- | :--- |
| `Missing required configuration '...'` | A value is not set. Set it as described in section 5 |
| `Cannot reach <url>` | Wrong URL, VPN or network problem |
| `Could not obtain an API token` | Check the login path, field names and token path against the Swagger |
| `Cannot log in to the inbox` | Use the Gmail **App Password**, not the normal password |
| `no visible element matching ...` | The page differs from what the test expects. Look at the screenshot, then adjust that one locator in the page class (usually only the label word in `fieldByLabel("...")`) |
| `No email to ... arrived within 180s` | The invite was not sent to that address, or the inbox settings are wrong |
| `No verification code matching ...` | The email wording differs. Change `inbox.code.regex` in `config.properties` |
| `No upload summary with added/rejected counts` | The summary wording differs. Adjust the matching pattern in `BulkUploadPage` |
| Many tests skipped | An earlier step failed. Fix the first red test |

## 10. Bugs and UX issues found

None reported yet because the QA environment has not been run. Problems found and fixed in this framework during review: assertions that could never fail, wrong bank fields (sort code instead of IBAN/BIC), invalid generated PPSNs, credentials in CI files, and Jenkins stages overwriting each other's results.

## 11. Written questions

**1. If a test fails in Jenkins, how do you find out whether the problem is the application or the test?**
Open the report: the message, the screenshot and the step that failed. A wrong status code, a missing row or an error banner points to the application. A timeout on a locator while the page looks normal points to the test. Then repeat it by hand or through the API. If the API check for the same rule also fails, it is the application.

**2. How would you run the UI tests in parallel on Jenkins agents and alert the team on failure?**
The driver is already thread-safe (`ThreadLocal`), so TestNG can run independent classes in parallel. In Jenkins I would use a `parallel` block with one stage per browser, or per suite, on separate agents or a Selenium Grid. For alerts, the `post { failure / unstable }` block sends an email, and Slack works the same way with a link to the report.

**3. You are the only tester on the product. How would you split effort between UI, API and mobile testing?**
About 50% API, 30% web UI, 20% mobile and exploratory. Business rules are cheapest and most stable at the API. UI automation covers only the key journeys. Mobile gets a small smoke test plus exploratory testing on real devices.

**4. How would you test payment reconciliation, where a bank file must match an employer's contribution payment?**
Create an employer with a known contribution amount, then feed bank files for each case: exact match, underpayment, overpayment, wrong reference, duplicate file. Check the result for each: status, amounts to the cent, no double allocation, and an audit record.

**5. How would you handle flaky tests caused by slow emails or third-party identity checks?**
Never fixed sleeps. Poll the inbox with a timeout and a unique address per run (done in this framework). Stub third-party identity checks in QA with fixed pass, fail and PEP personas, and keep a few real contract tests. At most one retry, only for known external steps, and report it.

**6. How would you manage test data so the QA environment does not fill up with junk?**
Prefix everything with `QA-AUTO-` plus a run id (done here). Clean up through the API after each run where possible, plus a scheduled job that removes old `QA-AUTO-` records. Reuse long-lived reference data instead of creating it every time.

**7. What would you add next if you had another week?**
Run on QA and record the evidence, add API cleanup of test data, automate invalid-IBAN and bad-upload-file cases, run browsers in parallel in Jenkins, and add the Appium registration smoke test.
