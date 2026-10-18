@echo off
REM ==============================================================================
REM Uplift QA Automation - Local Pipeline Runner (Simulates Jenkinsfile Stages)
REM Stages: 1. Checkout -> 2. Install -> 3. API tests -> 4. UI tests -> 5. Publish report
REM ==============================================================================

set ENVIRONMENT=QA
set BROWSER=chrome
set HEADLESS=true
set API_SECRET_TOKEN=qa-mock-bearer-token-uplift-2026
set ADMIN_USERNAME=admin@uplift.com
set ADMIN_PASSWORD=AdminPassword123!

if not exist "%JAVA_HOME%" set JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1
if "%JAVA_HOME:~-4%"=="\bin" set JAVA_HOME=%JAVA_HOME:~0,-4%
set PATH=%JAVA_HOME%\bin;C:\apache-maven-3.9.16\bin;%PATH%

if not "%~1"=="" set ENVIRONMENT=%~1
if not "%~2"=="" set BROWSER=%~2
if not "%~3"=="" set HEADLESS=%~3

echo ==============================================================================
echo                 UPLIFT QA AUTOMATION - LOCAL PIPELINE EXECUTION
echo ==============================================================================
echo Target Environment : %ENVIRONMENT%
echo Target Browser     : %BROWSER%
echo Headless Mode      : %HEADLESS%
echo ==============================================================================
echo.

echo [STAGE 1/5] Checkout SCM...
echo Checking repository status...
git status -s
if %ERRORLEVEL% NEQ 0 (
    echo Note: Git is running locally without remote, proceeding with current workspace.
)
echo [STAGE 1/5] Checkout completed successfully.
echo.

echo [STAGE 2/5] Install dependencies and compile classes...
call mvn clean compile test-compile -DskipTests=true
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Stage 2 (Install) failed!
    exit /b %ERRORLEVEL%
)
echo [STAGE 2/5] Install completed successfully.
echo.

echo [STAGE 3/5] API tests (REST Assured)...
call mvn test -DsuiteXmlFile=src/test/resources/testng-api.xml -Dapi.auth.token="%API_SECRET_TOKEN%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Stage 3 (API tests) failed!
    exit /b %ERRORLEVEL%
)
echo [STAGE 3/5] API tests passed!
echo.

echo [STAGE 4/5] UI tests (Selenium WebDriver)...
call mvn test -DsuiteXmlFile=src/test/resources/testng-smoke.xml -Dbrowser=%BROWSER% -Dheadless=%HEADLESS% -Dadmin.username="%ADMIN_USERNAME%" -Dadmin.password="%ADMIN_PASSWORD%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Stage 4 (UI tests) failed!
    exit /b %ERRORLEVEL%
)
echo [STAGE 4/5] UI tests passed!
echo.

echo [STAGE 5/5] Publish report...
if exist "reports\ExtentReport.html" (
    echo ExtentReports HTML dashboard generated at: reports\ExtentReport.html
    echo Surefire XML test results generated at:   target\surefire-reports\
    echo.
    echo ==============================================================================
    echo                 PIPELINE COMPLETED SUCCESSFULLY (All 5 Stages Passed)
    echo ==============================================================================
) else (
    echo [WARNING] Report file reports\ExtentReport.html was not found.
)
