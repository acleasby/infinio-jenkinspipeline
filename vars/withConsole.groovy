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
    sonarProject = sonarProject.isEmpty() ? "master" : sonarProject

    stage('Deploy console') {

        withEnv(["AUTO_PARAMETERS=managementvm.console_name=${consoleName},${testbedName}"]) {
            sh """#!/bin/bash
            source /auto/bin/env-setup.sh
            ManagementVm.py delete_all
            ManagementVm.py deploy
            """
        }

        if (collectCodeCoverage) {
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
        } else {
            echo "Skipping code coverage collection; no data was generated"
        }
    }
}
