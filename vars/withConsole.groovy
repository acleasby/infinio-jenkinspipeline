/**
 * This helper is used to deploy a console, optionally configuring it to collect code coverage.
 * @param args
 * @param body
 * @return
 */
def call(Map args, Closure body) {
    boolean collectCodeCoverage = args.get('collectCodeCoverage', false)
    String consoleName = args.get('consoleName')
    String testbedName = args.get('testbed')
    String sonarProject = args.get('sonarProject', "master")
    boolean useExistingConsole = args.get('useExistingConsole', false)

    sonarProject = sonarProject.isEmpty() ? "master" : sonarProject

    stage('Deploy or find console') {

        if (!useExistingConsole) {
            withEnv(["AUTO_PARAMETERS=managementvm.console_name=${consoleName},${testbedName}"]) {
                sh """#!/bin/bash
            source /auto/bin/env-setup.sh
            ManagementVm.py delete_all
            ManagementVm.py deploy
            """
            }
            env.consoleName = consoleName
        } else {
            withEnv(["AUTO_PARAMETERS=${testbedName}"]) {
                def consoleNames = sh(returnStdout: true, script: '''#!/bin/bash
                    source /auto/bin/env-setup.sh
                    ManagementVm.py find
            ''').trim()
                echo "Found consoles: $consoleNames"
                String[] names = consoleNames.substring(1, consoleNames.length() - 1).split(",").collect { name -> name.substring(name.indexOf("(") + 1, name.length() - 1) }
                if (names.length == 0) {
                    throw new IllegalStateException("Did not find any consoles")
                }
                env.consoleName = names[0]

                echo "Using existing console: ${names[0]}"
            }
        }


        if (collectCodeCoverage) {
            echo "Configuring for code coverage data collection"
            withEnv(["AUTO_PARAMETERS=${testbedName},coverage.enable_console=True"]) {
                sh '''#!/bin/bash
                source /auto/bin/env-setup.sh
                Triage.py setup'''
            }
        } else {
            echo "Code coverage data will not be collected"
        }
    }

    body()

    stage('Collect code coverage data') {

        if (collectCodeCoverage) {
            withEnv(["AUTO_PARAMETERS=${testbedName},coverage.enable_console=True,coverage.sonar_branch=${sonarProject}"]) {
                sh '''#!/bin/bash
                source /auto/bin/env-setup.sh
                Triage.py copy_console_coverage'''
            }
            echo "Code coverage data collected"
        } else {
            echo "Skipping code coverage collection; no data was generated"
        }
    }
}
