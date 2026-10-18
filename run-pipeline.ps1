# ==============================================================================
# Uplift QA Automation - Local Pipeline Runner (Simulates Jenkinsfile Stages)
# Stages: 1. Checkout -> 2. Install -> 3. API tests -> 4. UI tests -> 5. Publish report
# ==============================================================================

param (
    [string]$Environment = "QA",
    [string]$Browser = "chrome",
    [bool]$Headless = $true
)

$ErrorActionPreference = "Stop"

$validJdk = "C:\Program Files\Java\jdk-26.0.2.1"
if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME) -or ($env:JAVA_HOME -like "*\bin")) {
    $env:JAVA_HOME = $validJdk
}
$env:PATH = "$env:JAVA_HOME\bin;C:\apache-maven-3.9.16\bin;$env:PATH"

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "                UPLIFT QA AUTOMATION - LOCAL PIPELINE EXECUTION              " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "Target Environment : $Environment" -ForegroundColor Yellow
Write-Host "Target Browser     : $Browser" -ForegroundColor Yellow
Write-Host "Headless Mode      : $Headless" -ForegroundColor Yellow
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

$ApiSecretToken = "qa-mock-bearer-token-uplift-2026"
$AdminUsername  = "admin@uplift.com"
$AdminPassword  = "AdminPassword123!"

# Stage 1: Checkout
Write-Host "[STAGE 1/5] Checkout SCM..." -ForegroundColor Green
Write-Host "Simulating SCM checkout from workspace..." -ForegroundColor Gray
Write-Host "[STAGE 1/5] Checkout completed successfully." -ForegroundColor Green
Write-Host ""

# Stage 2: Install
Write-Host "[STAGE 2/5] Install dependencies and compile classes..." -ForegroundColor Green
& mvn clean compile test-compile -DskipTests=true
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Stage 2 (Install) failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[STAGE 2/5] Install completed successfully." -ForegroundColor Green
Write-Host ""

# Stage 3: API tests
Write-Host "[STAGE 3/5] API tests (REST Assured)..." -ForegroundColor Green
& mvn test "-DsuiteXmlFile=src/test/resources/testng-api.xml" "-Dapi.auth.token=$ApiSecretToken"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Stage 3 (API tests) failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[STAGE 3/5] API tests passed!" -ForegroundColor Green
Write-Host ""

# Stage 4: UI tests
Write-Host "[STAGE 4/5] UI tests (Selenium WebDriver)..." -ForegroundColor Green
& mvn test "-DsuiteXmlFile=src/test/resources/testng-smoke.xml" "-Dbrowser=$Browser" "-Dheadless=$Headless" "-Dadmin.username=$AdminUsername" "-Dadmin.password=$AdminPassword"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Stage 4 (UI tests) failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[STAGE 4/5] UI tests passed!" -ForegroundColor Green
Write-Host ""

# Stage 5: Publish report
Write-Host "[STAGE 5/5] Publish report..." -ForegroundColor Green
$ReportPath = Join-Path (Get-Location) "reports\ExtentReport.html"
if (Test-Path $ReportPath) {
    Write-Host "ExtentReports HTML dashboard generated at: $ReportPath" -ForegroundColor Cyan
    Write-Host "Surefire XML test results generated at:   target\surefire-reports\" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "==============================================================================" -ForegroundColor Green
    Write-Host "                 PIPELINE COMPLETED SUCCESSFULLY (All 5 Stages Passed)         " -ForegroundColor Green
    Write-Host "==============================================================================" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Report file $ReportPath was not found." -ForegroundColor Yellow
}
