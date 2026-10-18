pipeline {
    agent any

    // Nightly scheduled build trigger (runs between 2:00 AM and 2:59 AM)
    triggers {
        cron('H 2 * * *')
    }

    // Build Parameters for Environment and Browser selection
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['QA', 'STAGING', 'DEV'], description: 'Target Execution Environment')
        choice(name: 'BROWSER', choices: ['chrome', 'edge', 'firefox'], description: 'Browser for UI Automation Tests')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Execute UI tests in Headless mode')
    }

    environment {
        // Platform neutral tools: In Jenkins UI, configure 'Tools' with matching names (e.g. JDK_17 and Maven_3.9)
        // If agent already has tools in PATH, defaults are preserved
        PROJECT_DIR = "${env.WORKSPACE}"
        
        // Secure credentials retrieved from Jenkins Credentials Store (never committed to Git)
        // Set up in Jenkins -> Manage Jenkins -> Credentials -> System -> Global:
        // 1. Username with password: ID 'qa-admin-credentials'
        // 2. Secret text: ID 'qa-api-token'
        ADMIN_CREDS = credentials('qa-admin-credentials')
        API_SECRET_TOKEN = credentials('qa-api-token')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        ansiColor('xterm')
    }

    stages {
        // Stage 1: Checkout SCM repository
        stage('Checkout') {
            steps {
                echo "========================================="
                echo "Stage 1: Checking out code from repository"
                echo "Target Environment: ${params.ENVIRONMENT}"
                echo "Target Browser:     ${params.BROWSER}"
                echo "Headless Mode:      ${params.HEADLESS}"
                echo "========================================="
                checkout scm
            }
        }

        // Stage 2: Install dependencies & compile classes
        stage('Install') {
            steps {
                echo "========================================="
                echo "Stage 2: Compiling project and downloading dependencies"
                echo "========================================="
                // Cross-platform Maven execution (bat on Windows agents, sh on Linux/Mac agents)
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile test-compile -DskipTests=true'
                    } else {
                        bat 'mvn clean compile test-compile -DskipTests=true'
                    }
                }
            }
        }

        // Stage 3: API Automation Tests (Fast backend feedback)
        stage('API tests') {
            steps {
                echo "========================================="
                echo "Stage 3: Running REST Assured API tests"
                echo "========================================="
                script {
                    def mavenCmd = "mvn test -DsuiteXmlFile=src/test/resources/testng-api.xml -Dapi.auth.token=\"${API_SECRET_TOKEN}\""
                    if (isUnix()) {
                        sh mavenCmd
                    } else {
                        bat mavenCmd
                    }
                }
            }
        }

        // Stage 4: UI Automation Tests (End-to-End browser journeys)
        stage('UI tests') {
            steps {
                echo "========================================="
                echo "Stage 4: Running Selenium WebDriver UI tests"
                echo "Browser: ${params.BROWSER} | Headless: ${params.HEADLESS}"
                echo "========================================="
                script {
                    def mavenCmd = "mvn test -DsuiteXmlFile=src/test/resources/testng-smoke.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -Dadmin.username=\"${ADMIN_CREDS_USR}\" -Dadmin.password=\"${ADMIN_CREDS_PSW}\""
                    if (isUnix()) {
                        sh mavenCmd
                    } else {
                        bat mavenCmd
                    }
                }
            }
        }

        // Stage 5: Publish ExtentReports HTML dashboard
        stage('Publish report') {
            steps {
                echo "========================================="
                echo "Stage 5: Publishing ExtentReports and Test Artifacts"
                echo "========================================="
                // Publish ExtentReports HTML Dashboard using HTML Publisher plugin
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports',
                    reportFiles: 'ExtentReport.html',
                    reportName: 'Extent Test Report',
                    reportTitles: 'QA Automation Test Results'
                ])
            }
        }
    }

    post {
        always {
            echo "Archiving Surefire XML reports, failure screenshots, and execution logs..."
            // Archive screenshots, reports, and Surefire test XMLs
            archiveArtifacts artifacts: 'reports/**, target/surefire-reports/**', allowEmptyArchive: true

            // Publish TestNG/JUnit test results to Jenkins
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo "PIPELINE COMPLETED SUCCESSFULLY: All API and UI tests passed."
        }
        unstable {
            echo "PIPELINE UNSTABLE: One or more test assertions failed."
        }
        failure {
            echo "PIPELINE FAILED: Build error or critical test failure encountered."
            // Optional Email / Slack notification: Triggered only when SMTP/Slack plugin is configured
            script {
                // If emailext plugin is configured in Jenkins, uncomment:
                // emailext (
                //     subject: "FAILED: Job '${env.JOB_NAME}' [Build #${env.BUILD_NUMBER}]",
                //     body: "Build failed for environment ${params.ENVIRONMENT}. Check console output and Extent Report: ${env.BUILD_URL}",
                //     recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'CulpritsRecipientProvider']]
                // )
            }
        }
    }
}
