import com.cloudbees.groovy.cps.NonCPS

/**
 * Deploys a set of artifacts to Nexus using maven deploy-file
 * @param args
 */
def call(Map args) {
    assert args.containsKey("url"): "url must be provided"
    assert args.containsKey("repositoryId"): "repositoryId must be provided"
    assert args.containsKey("groupId"): "groupId must be provided"
    assert args.containsKey("version"): "version must be provided"
    assert args.containsKey("credentialsId"): "credentialsId is required"
    assert args.containsKey("artifacts"): "at least 1 artifact is required"

    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: args.credentialsId,
                      usernameVariable: 'NEXUS_DEPLOY_USERNAME', passwordVariable: 'NEXUS_DEPLOY_PASSWORD']]) {
        withMaven {
            args.artifacts.each { artifact ->
                validateArtifact(artifact)
                def urlWithCredentials = args.url.substring(0, args.url.indexOf("://") + 3) + NEXUS_DEPLOY_USERNAME + ":" + NEXUS_DEPLOY_PASSWORD + "@" + args.url.substring(args.url.indexOf("://")
                        + 3)
                def packaging = "-Dpackaging=" + (artifact.containsKey("packaging") ? artifact.packaging : getArtifactType(artifact.file))
                sh "mvn deploy:deploy-file -DgroupId=${args.groupId} -DartifactId=${artifact.artifactId} -Dversion=${args.version} -DgeneratePom=true ${packaging} -DrepositoryId=${args.repositoryId} " +
                        "-Durl=${urlWithCredentials} -Dfile=${artifact.file}"
            }
        }
    }
}

@NonCPS
void validateArtifact(def artifactParams) {
    assert artifactParams.containsKey("artifactId"): "artifactId must be provided"
    assert artifactParams.containsKey("file"): "file must be provided"
}

@NonCPS
String getArtifactType(String artifactFile) {
    return artifactFile.substring(artifactFile.lastIndexOf(".") + 1)
}