// Declarative pipeline: Checkout -> Install -> API tests -> UI tests -> Publish report
//
// Jenkins prerequisites (Manage Jenkins):
//   Plugins     : Pipeline, Git, Credentials Binding, HTML Publisher, JUnit, Mailer (for the optional email)
//   Agent tools : JDK 17+, Maven 3.9+ and Chrome/Edge/Firefox on the PATH of the build agent
//   Credentials : "Username with password" entries with these IDs (values never live in the repo):
//       qa-admin-credentials   Admin portal test login
//       qa-api-credentials     API credentials from the Swagger documentation
//       qa-test-inbox          Test inbox (e.g. Gmail address + App Password) for invite emails
//       qa-employer-admin      Activated QA-AUTO employer admin, used by the API stage's UI cross-check
//   QA URLs     : set ADMIN_PORTAL_URL, EMPLOYER_PORTAL_URL and API_BASE_URL as global environment
//                 variables (or commit them in src/test/resources/environments/qa.properties)

pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa'], description: 'Target environment (a new one = a new file in src/test/resources/environments/)')
        choice(name: 'BROWSER', choices: ['chrome', 'edge', 'firefox'], description: 'Browser for UI tests')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run the browser headless')
        string(name: 'NOTIFY_EMAIL', defaultValue: '', description: 'Optional: email address(es) notified when the build is failed or unstable')
    }

    triggers {
        // Nightly run between 02:00 and 02:59
        cron('H 2 * * *')
        // Part F: build on every push (polls the repository every ~5 minutes; a GitHub webhook can replace this)
        pollSCM('H/5 * * * *')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Environment: ${params.ENVIRONMENT} | Browser: ${params.BROWSER} | Headless: ${params.HEADLESS}"
                checkout scm
            }
        }

        stage('Install') {
            steps {
                runMaven('clean test-compile')
            }
        }

        stage('API tests') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'qa-api-credentials', usernameVariable: 'API_AUTH_USERNAME', passwordVariable: 'API_AUTH_PASSWORD'),
                    usernamePassword(credentialsId: 'qa-employer-admin', usernameVariable: 'EMPLOYER_ADMIN_EMAIL', passwordVariable: 'EMPLOYER_ADMIN_PASSWORD')
                ]) {
                    runMaven('test -DsuiteXmlFile=src/test/resources/testng-api.xml' +
                             ' -Dreport.path=reports/api/ExtentReport.html -Dsurefire.reports.dir=target/surefire-reports/api')
                }
            }
        }

        stage('UI tests') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'qa-admin-credentials', usernameVariable: 'ADMIN_USERNAME', passwordVariable: 'ADMIN_PASSWORD'),
                    usernamePassword(credentialsId: 'qa-test-inbox', usernameVariable: 'INBOX_USERNAME', passwordVariable: 'INBOX_PASSWORD')
                ]) {
                    runMaven('test -DsuiteXmlFile=src/test/resources/testng-ui.xml' +
                             ' -Dreport.path=reports/ui/ExtentReport.html -Dsurefire.reports.dir=target/surefire-reports/ui')
                }
            }
        }

        stage('Publish report') {
            steps {
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports',
                    reportFiles: 'api/ExtentReport.html, ui/ExtentReport.html',
                    reportName: 'Extent Test Report',
                    reportTitles: 'API tests, UI tests'
                ])
            }
        }
    }

    post {
        always {
            // Marks the build UNSTABLE when any test failed (Maven itself is told not to stop on test failures)
            junit testResults: 'target/surefire-reports/**/TEST-*.xml', allowEmptyResults: true
            // HTML reports, failure screenshots (reports/screenshots) and per-test logs (*-output.txt)
            archiveArtifacts artifacts: 'reports/**, target/surefire-reports/**', allowEmptyArchive: true
        }
        unstable {
            notifyTeam('UNSTABLE: one or more tests failed')
        }
        failure {
            notifyTeam('FAILED: build or pipeline error')
        }
    }
}

// Runs Maven with the build parameters. Test failures do not stop the pipeline: the junit step
// marks the build UNSTABLE instead, so the UI stage still runs and every report is published.
def runMaven(String goals) {
    String cmd = "mvn -B ${goals} -Denv=${params.ENVIRONMENT} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}" +
                 ' -Dmaven.test.failure.ignore=true -Dmaven.test.redirectTestOutputToFile=true'
    if (isUnix()) {
        sh cmd
    } else {
        bat cmd
    }
}

// Bonus: failure notification by email (requires the Mailer plugin and an SMTP server in Jenkins)
def notifyTeam(String status) {
    if (params.NOTIFY_EMAIL?.trim()) {
        try {
            mail to: params.NOTIFY_EMAIL,
                 subject: "${status} - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "${status}\nEnvironment: ${params.ENVIRONMENT}, browser: ${params.BROWSER}\n" +
                       "Report: ${env.BUILD_URL}Extent_20Test_20Report/\nConsole: ${env.BUILD_URL}console"
        } catch (Exception e) {
            // A missing SMTP server must never change the build result
            echo "Email not sent (configure SMTP under Manage Jenkins > System > E-mail Notification): ${e.message}"
        }
    }
}
