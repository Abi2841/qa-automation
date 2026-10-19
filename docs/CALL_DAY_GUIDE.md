# Call-day guide: from "here are the URLs" to a green run

## A. Do before the call
- [ ] `java -version`, `mvn -v` and Chrome work (`mvn -B clean test-compile` succeeds).
- [ ] Gmail test inbox ready: 2-step verification on, **App Password** created (16 characters). Keep it for `inbox.password`.
- [ ] `src/test/resources/secrets.properties` created from the `.example` file (it is gitignored).
- [ ] Jenkins job `Selenium-Java-Pipeline` opens at http://localhost:8080 and the four credential IDs exist (see README section 5).
- [ ] Screen sharing works and VS Code shows the terminal.

## B. Ask these on the call (write the answers down)
1. Admin portal URL, Employer portal URL, Admin login (username + password).
2. API base URL and the **Swagger URL**.
3. In Swagger: the **login endpoint** path, request body field names, and where the token is in the response.
4. In Swagger: the **create / get employee** path, the JSON field names (e.g. `ppsn`, `mobileNumber`?), the success response shape, and the error shape for a bad PPSN.
5. Does the API need an **employer id** with the employee?
6. API credentials: the same as the admin login, or separate?
7. How the invite email looks: is the verification code in the body? Which sender?
8. Is the Mailinator or Gmail "+alias" approach fine? (Gmail is already set up.)

## C. Where each answer goes

| You were given | Put it here |
|---|---|
| Admin portal URL | `environments/qa.properties` → `admin.portal.url` |
| Employer portal URL | `environments/qa.properties` → `employer.portal.url` |
| API base URL | `environments/qa.properties` → `api.base.url` |
| Login endpoint path | `environments/qa.properties` → `api.auth.endpoint` |
| Login body field names | `api.auth.username.field`, `api.auth.password.field` |
| Token field in the response | `api.auth.token.path` (use dots for nested, e.g. `data.token`) |
| Employees endpoint path | `api.employees.endpoint` |
| Employee id field in the response | `api.employee.id.path` |
| Employer id (if needed) | `api.employer.id` |
| Admin username / password | `secrets.properties` → `admin.username`, `admin.password` |
| API username / password | `secrets.properties` → `api.auth.username`, `api.auth.password` |
| Gmail address + App Password | `secrets.properties` → `inbox.address`, `inbox.username`, `inbox.password` |
| Expected employer status text | `employer.expected.status` (optional, e.g. `Active`) |

Alternative with no file edits: pass any value as `"-Dkey=value"` on the Maven command line, or set the environment variable (dots become underscores, upper case: `admin.portal.url` → `ADMIN_PORTAL_URL`).

If the **response schema** differs, edit `src/test/resources/schemas/employee-response.schema.json` (required fields and types). If the JSON **field names** differ from the model, change them in `EmployeeModel.java` with `@JsonProperty("realName")`.

## D. Run in this order

```powershell
# 1. Are all values set, and do they work? (values, URLs, browser, API login, inbox)
mvn test "-DsuiteXmlFile=src/test/resources/testng-preflight.xml"

# 2. Admin part only, browser visible, so you can watch and fix locators
mvn test "-Dtest=AdminPortalJourneyTest" "-Dheadless=false"

# 3. The whole UI journey + Part C
mvn test "-DsuiteXmlFile=src/test/resources/testng-ui.xml" "-Dheadless=false"

# 4. API only
mvn test "-DsuiteXmlFile=src/test/resources/testng-api.xml"

# 5. Everything with one command (final report)
mvn clean test
```

Part C and the API cross-check use the employer admin created by the journey in the **same run**. To run Part C alone (`mvn test "-Dtest=EmployeeValidationTest"`), set `employer.admin.email` and `employer.admin.password` in `secrets.properties`.

## E. When a step fails: fix the locator

The failure message looks like `no visible element matching By.xpath: ...`. Open the report, look at the screenshot, then edit **one line** in the page class:

| Screen | File | What to edit |
|---|---|---|
| Admin / Employer login | `AdminLoginPage`, `EmployerLoginPage` | `usernameInput`, `passwordInput`, `loginButton` |
| Menu and logout | `PortalPage` | `logoutAction`, `userMenuToggles`; menu text is in `AdminDashboardPage` / `EmployerDashboardPage` (`"Employers"`, `"Employees"`) |
| Employers list | `EmployersPage` | `addEmployerButton`, `searchInput` |
| Add employer form | `CreateEmployerPage` | each `fieldByLabel("...")`, `addressSuggestion`, `saveButton` |
| Employer admins | `EmployerDetailsPage` | `adminsSection`, `addAdminButton`, field labels, `saveAdminButton`; PEP question text in `answerPepQuestion` |
| Invite / first login | `EmployerActivationPage` | `codeInput`, `submitButton` |
| Employees | `EmployeesPage`, `AddEmployeePage` | `addEmployeeButton`, `bulkUploadButton`, field labels, `saveButton` |
| Bulk upload | `BulkUploadPage` | `downloadTemplateLink`, `uploadSubmitButton`; the summary wording regex (`ADDED` / `REJECTED`) |
| Success / error messages | `BasePage` | `SUCCESS_MESSAGE`, `VALIDATION_MESSAGE` |

**Fastest fix:** most fields are found by their label text, so usually only the word changes. Example: the screen says "Registered number" instead of "CRN" → change `fieldByLabel("crn", "registration number")` to `fieldByLabel("registered number")`.

If the label approach doesn't work, right-click the element in Chrome → Inspect, and use a CSS selector with the id or name, for example `By.cssSelector("#companyName")`.

## F. Quick problem map

| Message | Meaning | Fix |
|---|---|---|
| `Missing values: ...` (preflight) | A value from section C isn't set | Set it where the message says |
| `Cannot reach <url>` | Wrong URL, VPN or network | Check the URL in a browser |
| `Could not obtain an API token: HTTP 4xx` | Wrong endpoint, body field names or credentials | Re-check section C against Swagger |
| `Cannot log in to the inbox` | Gmail needs an App Password | Re-create it; use it, not your normal password |
| `no visible element matching ...` | Locator mismatch | Section E |
| `No email to ... arrived within 180s` | Invite wasn't sent to that address, or the inbox can't be read | Check the admin's email on screen; check `inbox.*` |
| `No verification code matching ...` | Different email wording | Change `inbox.code.regex` in `config.properties` |
| `No upload summary with added/rejected counts` | Different summary wording | Adjust the `ADDED` / `REJECTED` regex in `BulkUploadPage` |
| Many tests **skipped** | Earlier step failed on purpose | Fix the first red one |

## G. Before you stop sharing your screen
- [ ] Final `mvn clean test` done; save `reports/ExtentReport.html`.
- [ ] Commit the config changes that are **not secret** (`qa.properties`). Never commit `secrets.properties`.
- [ ] Jenkins build run once; take the three screenshots.
- [ ] Note any bugs or UX oddities you saw (the brief asks for them) in README section 7.
