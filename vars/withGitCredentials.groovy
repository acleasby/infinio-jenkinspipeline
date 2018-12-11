def call(Map args, Closure body) {
    assert args.containsKey("credentialsId"): "credentialsId must be provided"
    String credentialsId = args.get("credentialsId")

    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: credentialsId,
                      usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {

        sh """
git config user.email "${GIT_USERNAME}@infinio.com"
git config user.name "${GIT_USERNAME}"
git config credential.helper cache
"""

        sh "echo 'protocol=https\nhost=github.com\nusername=${GIT_USERNAME}\npassword=${GIT_PASSWORD}\n\n' | git credential approve "

        body()
    }
}
