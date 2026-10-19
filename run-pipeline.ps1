# ==============================================================================
# Uplift QA Automation - Local Pipeline Runner (mirrors the Jenkinsfile stages)
# Stages: 1. Checkout -> 2. Install -> 3. API tests -> 4. UI tests -> 5. Publish report
#
# Credentials are NOT stored here: put them in src/test/resources/secrets.properties
# (copy secrets.properties.example) or set environment variables (ADMIN_PASSWORD, ...).
#
# Usage: .\run-pipeline.ps1 [-Environment qa|demo] [-Browser chrome|edge|firefox] [-Headless $true|$false]
# ==============================================================================

param (
    [string]$Environment = "qa",
    [string]$Browser = "chrome",
    [bool]$Headless = $true
)

# Use JAVA_HOME only if it points at a real JDK folder (some setups wrongly end it with \bin)
if ($env:JAVA_HOME -and ($env:JAVA_HOME -like "*\bin")) {
    $env:JAVA_HOME = Split-Path $env:JAVA_HOME -Parent
}

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "                UPLIFT QA AUTOMATION - LOCAL PIPELINE EXECUTION              " -ForegroundColor Cyan
Write-Host "Environment: $Environment | Browser: $Browser | Headless: $Headless" -ForegroundColor Yellow
Write-Host "==============================================================================" -ForegroundColor Cyan

$common = @("-B", "-Denv=$Environment", "-Dbrowser=$Browser", "-Dheadless=$Headless", "-Dmaven.test.failure.ignore=true")
$testsFailed = $false

function Test-StageResults([string]$Dir) {
    $failed = Select-String -Path "$Dir\TEST-*.xml" -Pattern '(failures|errors)="[1-9]' -Quiet -ErrorAction SilentlyContinue
    return [bool]$failed
}

Write-Host "[STAGE 1/5] Checkout: using the current workspace" -ForegroundColor Green
git log -1 --oneline

Write-Host "[STAGE 2/5] Install: compile and resolve dependencies" -ForegroundColor Green
& mvn -B clean test-compile
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] Install failed" -ForegroundColor Red; exit $LASTEXITCODE }

Write-Host "[STAGE 3/5] API tests (REST Assured)" -ForegroundColor Green
& mvn test @common "-DsuiteXmlFile=src/test/resources/testng-api.xml" "-Dreport.path=reports/api/ExtentReport.html" "-Dsurefire.reports.dir=target/surefire-reports/api"
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] API stage could not run" -ForegroundColor Red; exit $LASTEXITCODE }
if (Test-StageResults "target\surefire-reports\api") { $testsFailed = $true; Write-Host "[UNSTABLE] API tests failed" -ForegroundColor Yellow }

Write-Host "[STAGE 4/5] UI tests (Selenium WebDriver)" -ForegroundColor Green
& mvn test @common "-DsuiteXmlFile=src/test/resources/testng-ui.xml" "-Dreport.path=reports/ui/ExtentReport.html" "-Dsurefire.reports.dir=target/surefire-reports/ui"
if ($LASTEXITCODE -ne 0) { Write-Host "[ERROR] UI stage could not run" -ForegroundColor Red; exit $LASTEXITCODE }
if (Test-StageResults "target\surefire-reports\ui") { $testsFailed = $true; Write-Host "[UNSTABLE] UI tests failed" -ForegroundColor Yellow }

Write-Host "[STAGE 5/5] Publish report" -ForegroundColor Green
Write-Host "API report : reports\api\ExtentReport.html"
Write-Host "UI report  : reports\ui\ExtentReport.html"
Write-Host "Screenshots: reports\screenshots\"

if ($testsFailed) {
    Write-Host "PIPELINE UNSTABLE: one or more tests failed - see the reports above" -ForegroundColor Yellow
    exit 1
}
Write-Host "PIPELINE PASSED" -ForegroundColor Green
