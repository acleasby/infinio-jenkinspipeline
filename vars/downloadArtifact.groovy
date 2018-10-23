/**
 * Downloads an artifact from Nexus to a local file
 * @param args
 */
def call(Map args) {
    assert args.containsKey("nexus"): "nexus must be provided"
    assert args.containsKey("repository"): "repository must be provided"
    assert args.containsKey("groupId"): "groupId must be provided"
    assert args.containsKey("artifactId"): "artifactId must be provided"
    assert args.containsKey("version"): "version must be provided"
    assert args.containsKey("extension"): "extension must be provided"

    String artifactPath = args.groupId.replace(".", "/") + "/" + args.artifactId
    String artifactFile = "${args.artifactId}-${args.version}.${args.extension}"
    URL artifactUrl = new URL("https://${args.nexus}/content/repositories/${args.repository}/${artifactPath}/${args.version}/${artifactFile}")

    String localFile = args.containsKey("file") ? args.file : artifactFile
    sh("curl '${artifactUrl}' -L -o ${localFile}")
}