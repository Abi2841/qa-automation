@echo off
REM ==============================================================================
REM Uplift QA Automation - Local Pipeline Runner (Windows cmd wrapper)
REM Delegates to run-pipeline.ps1 so both runners behave identically.
REM Usage: run-pipeline.bat [qa^|demo] [chrome^|edge^|firefox] [true^|false]
REM ==============================================================================

set ENVIRONMENT=qa
set BROWSER=chrome
set HEADLESS=$true
if not "%~1"=="" set ENVIRONMENT=%~1
if not "%~2"=="" set BROWSER=%~2
if /i "%~3"=="false" set HEADLESS=$false

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-pipeline.ps1" -Environment %ENVIRONMENT% -Browser %BROWSER% -Headless %HEADLESS%
exit /b %ERRORLEVEL%
