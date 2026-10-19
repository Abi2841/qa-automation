# Workplace Pension Platform (Ireland) - QA Automation Framework

Java 17 · Selenium · TestNG · REST Assured · ExtentReports · Maven · Jenkins

**Done by Arun**

## 1. What this project does

It automates one journey in the pension platform, through the UI and the API:

**Admin creates an employer → Admin adds an employer admin → Employer admin adds employees → Employees appear in the list and get an invite.**

```
Admin portal                          Employer portal                      API
login                                 open invite email (code + link)      get token / 401 without token
create employer (IBAN, BIC...)   ──►  set password, log in            ──►  create employee (201 + schema)
add employer admin (PEP...)           add 1 employee                       read it back
        │                             bulk upload CSV (3 good, 2 bad)      bad PPSN -> 400
        └── invite email ──► Gmail    16 invalid-data checks               employee visible in the UI
                                      log out
                    Every test ──► HTML report (+ screenshot when it fails)
```

## 2. Status (honest)

| Part | What | Status |
| :--- | :--- | :--- |
| A | 24 test cases and automate-vs-manual reasons (`docs/TEST_CASES_PART_A.md`) | Done |
| B | UI journey (Admin portal, invite email, Employer portal, CSV upload, logout) | Written, **not yet run on QA** |
| C | One data-driven test, 16 scenarios in JSON | Written, **not yet run on QA** |
| D | 6 API checks with schema validation | Written, **not yet run on QA** |
| E | Jenkinsfile: 5 stages, parameters, credentials, report, nightly | Written, **not yet run in Jenkins** |
| F | Build on push, invite-email check | Written. Appium and parallel runs not done |
| - | Framework self-test (config, data, CSV, browser, report, screenshots) | **Passing: 10 of 10** |

The QA URLs, login and Swagger were not available while building, so the journey, validation and API tests are written from the application guide and still need their first run on QA. The first run will mostly be small locator fixes in the page classes. Until the values are set, each suite stops within seconds with a message naming what is missing.

## 3. How it is organised

| Folder | What is in it |
| :--- | :--- |
| `src/main/java/.../pages` | Page Objects: the only place with locators (label-based, explicit waits) |
| `src/main/java/.../api` | API helper (token, create, get) |
| `src/main/java/.../config` | `ConfigReader`: reads settings (command line > environment variable > `secrets.properties` > environment file > `config.properties`) |
| `src/main/java/.../data` | Unique `QA-AUTO-` data, valid PPSN and IBAN generators, CSV builder from the portal template |
| `src/main/java/.../utils` | Waits (no `Thread.sleep`), screenshots, Gmail inbox reader |
| `src/main/java/.../reporting` | ExtentReports listener |
| `src/test/java/.../tests` | Preflight, Admin journey, Employer journey, data-driven validation, API tests |
| `src/test/resources` | `environments/qa.properties`, `secrets.properties.example`, TestNG suites, test data, JSON schema |
| `Jenkinsfile`, `run-pipeline.ps1` | Pipeline and a local copy of it |
| `docs/` | Test cases and `CALL_DAY_GUIDE.md` |

## 4. Setup

1. Install JDK 17+, Maven 3.9+ and Chrome. Check with `java -version` and `mvn -v`.
2. Open `src/test/resources/environments/qa.properties` and fill in the three URLs and the two endpoint paths (from Swagger).
3. Copy `src/test/resources/secrets.properties.example` to `secrets.properties` (it is not uploaded to Git) and fill in the admin login, API login, and test inbox.
4. Test inbox: a Gmail account with 2-step verification and an [App Password](https://myaccount.google.com/apppasswords). Invites go to `you+qa-auto-...@gmail.com`, so each run finds its own email.

Only the QA environment is used. Everything created starts with `QA-AUTO-`.

## 5. How to run

In PowerShell, keep the quotes around `-D` options.

| What | Command |
| :--- | :--- |
| Check all values work (run this first) | `mvn test "-DsuiteXmlFile=src/test/resources/testng-preflight.xml"` |
| **Everything, one command** | `mvn clean test` |
| UI only | `mvn test "-DsuiteXmlFile=src/test/resources/testng-ui.xml"` |
| API only | `mvn test "-DsuiteXmlFile=src/test/resources/testng-api.xml"` |
| Watch the browser | add `"-Dheadless=false"` |
| Framework self-test (no QA needed) | `mvn test "-Denv=demo" "-DsuiteXmlFile=src/test/resources/testng-smoke.xml"` |

Results: `reports/ExtentReport.html` (report), `reports/screenshots/` (failure screenshots), `target/surefire-reports/` (raw results).

On a call, follow `docs/CALL_DAY_GUIDE.md`: what to ask, where each value goes, and how to fix a locator.

## 6. Jenkins

Stages: **Checkout → Install → API tests → UI tests → Publish report**.

- Parameters: environment, browser, headless, optional notify email.
- Passwords come from the Jenkins credentials store (`qa-admin-credentials`, `qa-api-credentials`, `qa-test-inbox`, `qa-employer-admin`), never from the repo.
- API and UI stages each write their own report. The HTML report is shown on the build page, and screenshots and logs are saved as build artifacts.
- A failing test makes the build **UNSTABLE**. A build or pipeline error makes it **FAILED**.
- Runs nightly (`cron('H 2 * * *')`) and checks GitHub for new pushes every ~5 minutes.

## 7. Assumptions

1. Field names on screen are close to the application guide. If not, only the page classes change.
2. The address lookup shows suggestions and the first one is fine.
3. Economic status and occupation are dropdowns. If my value is missing, the first real option is used and noted in the report.
4. The invite email has the code next to the word "code" and a link to the Employer portal.
5. The upload summary shows words like "3 added / 2 rejected" with a reason per rejected row.
6. The API uses the same field names as the model. Paths and field names can be changed in `qa.properties`.

## 8. Bugs and UX issues

None to report yet, because the QA environment has not been run. Problems found and fixed in my own framework during review: assertions that could never fail, wrong bank fields (UK sort code instead of IBAN/BIC), invalid generated PPSNs, credentials in CI files, and Jenkins stages overwriting each other's results.

## 9. Written questions

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
