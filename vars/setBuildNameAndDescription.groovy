/**
 * Sets BUILD_IDENT if not already set, also sets Jenkins build name and description.
 * @return
 */
def call() {
    String buildIdent = env['BUILD_IDENT'] ?: ""
    String gitBranch = env['GIT_BRANCH'] ?: "master"
    String testSuite = env['testSuite'] ?: null

    def buildNumber = currentBuild.number

    if (buildIdent.isEmpty()) {
        buildIdent = readFile(file: "RELEASENAME").trim() + ".dev";
    }

    env.BUILD_IDENT = buildIdent

    "#" + currentBuild.number + " " + "$buildIdent-$gitBranch"
    String buildDescription = "build #: $buildNumber, branch: ${gitBranch}" + (testSuite != null ? ", suite: $testSuite" : "")

    // Set Jenkins build name and description
    currentBuild.displayName = buildName
    currentBuild.description = buildDescription

    echo "Set build name=$buildName, description=$buildDescription"
}
