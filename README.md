# QA Automation Framework (Phase 2 Skeleton)

A modular, enterprise-ready Test Automation Framework built in **Java** using **Maven**, **Selenium WebDriver 4**, **TestNG**, **REST Assured**, and **ExtentReports 5**.

---

## 📁 Project Architecture & Package Structure

```
c:\Users\Arunnivetha\Uplift
├── pom.xml                                  # Maven project configuration & dependencies
├── .gitignore                               # Excludes build artifacts, secrets, and reports
├── README.md                                # Framework documentation and run instructions
├── docs
│   └── TEST_CASES_PART_A.md                 # Part A: Test Case Specification (22 scenarios)
├── src
│   ├── main
│   │   └── java
│   │       └── com
│   │           └── uplift
│   │               ├── api                  # API client wrappers & HTTP utilities
│   │               │   └── ApiClient.java
│   │               ├── base                 # Core lifecycle & driver management
│   │               │   ├── BaseApiTest.java
│   │               │   ├── BaseTest.java
│   │               │   └── DriverFactory.java
│   │               ├── config               # Hierarchical configuration & secrets loader
│   │               │   └── ConfigReader.java
│   │               ├── data                 # Test data loaders & dynamic CSV generators
│   │               │   ├── CsvTestDataGenerator.java
│   │               │   └── TestDataManager.java
│   │               ├── models               # Dynamic POJO data generators (QA-AUTO-)
│   │               │   ├── EmployeeModel.java
│   │               │   ├── EmployerAdminModel.java
│   │               │   └── EmployerModel.java
│   │               ├── pages                # Page Object Model (POM) layer
│   │               │   ├── BasePage.java
│   │               │   ├── admin            # Admin Portal Pages
│   │               │   │   ├── AdminDashboardPage.java
│   │               │   │   ├── AdminLoginPage.java
│   │               │   │   ├── CreateEmployerPage.java
│   │               │   │   ├── EmployerDetailsPage.java
│   │               │   │   └── EmployersPage.java
│   │               │   └── employer         # Employer Portal Pages
│   │               │       ├── AddEmployeePage.java
│   │               │       ├── BulkUploadPage.java
│   │               │       ├── EmployerDashboardPage.java
│   │               │       ├── EmployerLoginPage.java
│   │               │       └── EmployeesPage.java
│   │               ├── reporting            # ExtentReports 5 initialization & listeners
│   │               │   ├── ExtentManager.java
│   │               │   └── ExtentTestListener.java
│   │               └── utils                # Reusable waits, screenshots & inbox handlers
│   │                   ├── EmailInboxHandler.java
│   │                   ├── ScreenshotUtils.java
│   │                   └── WaitUtils.java
│   └── test
│       ├── java
│       │   └── com
│       │       └── uplift
│       │           └── tests                # Test suites & journey tests
│       │               ├── FrameworkSmokeTest.java
│       │               ├── api
│       │               │   └── SmokeApiTest.java
│       │               └── ui
│       │                   ├── AdminPortalJourneyTest.java
│       │                   ├── EmployerPortalJourneyTest.java
│       │                   └── SmokeUiTest.java
│       └── resources
│           ├── config.properties            # QA environment settings (URL, browser, timeouts)
│           ├── secrets.properties.example   # Template for local credentials (never committed)
│           ├── testng-smoke.xml             # TestNG smoke execution suite
│           └── testdata
│               ├── bulk-employees-5records.csv # 5-record CSV fixture (3 valid, 2 invalid)
│               └── sample-data.json         # Sample test data fixture
```

---

## ⚙️ Configuration & Secrets Management

Configuration resolution follows a strict priority hierarchy:
1. **System Properties** (`-Dbrowser=chrome`, `-Dheadless=false`, `-Dbase.url=...`)
2. **Environment Variables** (`BROWSER`, `BASE_URL`, `API_AUTH_TOKEN`)
3. **Local Secrets File** (`src/test/resources/secrets.properties` - gitignored)
4. **Classpath Defaults** (`src/test/resources/config.properties`)

### Handling Passwords and Secrets
1. Copy `src/test/resources/secrets.properties.example` to `src/test/resources/secrets.properties`.
2. Populate the actual credentials locally.
3. Because `secrets.properties` is listed in `.gitignore`, it will **never** be committed into source control.
4. Alternatively, pass sensitive credentials at runtime:
   ```powershell
   mvn test -Dapp.password="your_password" -Dapi.auth.token="your_token"
   ```

---

## 🚀 Execution Commands

### Prerequisites
Ensure `JAVA_HOME` points to your JDK directory:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
```

### Run All Smoke Tests
```powershell
mvn clean test
```

### Run with a Specific Browser (e.g., Edge or Chrome)
```powershell
mvn test -Dbrowser=edge
mvn test -Dbrowser=chrome
```

### Run in Headed (Visual) Mode
```powershell
mvn test -Dheadless=false
```

### Run Against a Custom Environment / Base URL
```powershell
mvn test -Dbase.url="https://the-internet.herokuapp.com" -Dapi.base.url="https://reqres.in/api"
```

---

## 📊 Test Reports

After test execution, the standalone ExtentReports HTML report is available at:
- `reports/ExtentReport.html`

Open it in any web browser to view rich test statuses, execution metadata, and screenshots.

---

## 🏗️ Jenkins CI/CD Declarative Pipeline (`Jenkinsfile`)

The framework includes a production-grade declarative pipeline (`Jenkinsfile`) implementing the required 5 stages:

1. **Checkout:** Clones code from Git repository.
2. **Install:** Downloads dependencies and compiles classes (`mvn clean compile test-compile -DskipTests=true`).
3. **API tests:** Executes fast REST Assured backend suite (`testng-api.xml`).
4. **UI tests:** Executes headless Selenium WebDriver browser journeys (`testng-smoke.xml`).
5. **Publish report:** Publishes `reports/ExtentReport.html` via the HTML Publisher plugin and archives Surefire test XMLs.

### Nightly Trigger
Configured with cron expression:
```groovy
triggers {
    cron('H 2 * * *') // Runs nightly between 2:00 AM and 2:59 AM
}
```

### Jenkins Setup & Credentials Configuration
1. Install recommended plugins: **Pipeline**, **Git**, **HTML Publisher**, and **JUnit**.
2. Go to **Manage Jenkins** → **Credentials** → **System** → **Global credentials**:
   - Add **Username with password** with ID: `qa-admin-credentials`
   - Add **Secret text** with ID: `qa-api-token`
3. Create a **New Item** → **Pipeline** → Point SCM to your Git repository URL.
4. Click **Build with Parameters** to choose Environment (`QA`, `STAGING`, `DEV`), Browser (`chrome`, `edge`, `firefox`), and Headless toggle.

