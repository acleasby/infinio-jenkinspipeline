def call(Map args) {
    assert args.containsKey("tag"): "tag must be provided"
    assert args.containsKey("credentialsId"): "credentialsId must be provided"
    String tag = args.get("tag")
    String message = args.get("message")
    String credentialsId = args.get("credentialsId")

    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: credentialsId,
                      usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
        sh("""
git config user.email "${GIT_USERNAME}@infinio.com"
git config user.name "${GIT_USERNAME}"
git tag -af ${tag} -m '${message}'
git push -f --tags
""")
    }
}