/**
 * Deletes an artifact from Nexus
 * @param args
 */
def call(Map args) {
    assert args.containsKey("username"): "username must be provided"
    assert args.containsKey("password"): "password must be provided"
    assert args.containsKey("nexus"): "nexus must be provided"
    assert args.containsKey("repository"): "repository must be provided"
    assert args.containsKey("groupId"): "groupId must be provided"
    assert args.containsKey("artifactId"): "artifactId must be provided"

    String artifactPath = args.groupId.replace(".", "/") + "/" + args.artifactId
    String artifactUrl = "https://${args.nexus}/service/local/repositories/${args.repository}/content/${artifactPath}"

    sh("curl -S -s --request DELETE --user \"${args.username}:${args.password}\" ${artifactUrl}")

    sh("curl -S -s --request DELETE  --user \"${args.username}:${args.password}\"  --silent http://${args.nexus}/service/local/metadata/repositories/${args.repository}/content")
}