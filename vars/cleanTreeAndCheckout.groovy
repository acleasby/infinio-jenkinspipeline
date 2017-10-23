import com.cloudbees.groovy.cps.NonCPS

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * Step to checkout a given git commit into an optionally specified directory.  Cleans target dir first.
 * @param args
 * @param body
 * @return
 */
def call(Map args, Closure body) {
    String localRepo = args.get("localRepo", ".repository")
    String gitBranch = args.get("ref", "master")
    String url = args.get("gitUrl", "https://github.com/infinio/mgmt.git")
    String credentialsId = args.get("credentialsId", "53750c83-8c06-4c1f-8aae-e5a0aecab4cf")
    String excludeArg = ""
    if (args.get("excludedCleanDirs") != null && !args.get("excludedCleanDirs").trim().isEmpty()) {
        String[] excludes = args.get("excludedCleanDirs", "").split(",")
        excludeArg = excludes.toList().stream().collect(Collectors.joining(" ", "-e ", ""))
    }

    File workingDirectory = createWorkingPath(args.get("workDir", "."))
    dir(workingDirectory.toString()) {
        echo "Checking out code to ${workingDirectory}"
        stage("Checkout code") {
            if (localRepo != null) {
                echo "Cleaning tree, excluding local repo ${localRepo}"
                sh "git clean -fdx -e ${localRepo} ${excludeArg} || /bin/true"
            }
            checkout poll: false, scm: [
                    $class           : 'GitSCM',
                    branches         : [[name: "${gitBranch}"]],
                    userRemoteConfigs: [[
                                                url          : "${url}",
                                                credentialsId: "${credentialsId}",
                                                refspec      : '+refs/heads/*:refs/remotes/origin/*'
                                        ]]
            ]
        }

        body()
    }
}

@NonCPS
File createWorkingPath(String workingDirectoryName) {
    Path workingPath = Paths.get(workingDirectoryName)
    if (!workingPath.toFile().exists()) {
        workingPath.toFile().mkdirs()
    }

    return workingPath.toFile()
}
