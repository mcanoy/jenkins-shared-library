// When using the non-declarative pipeline, git env variables need to be set through scm checkout
def call(env) {

    def patch = [
            spec: [
                output : [
                    imageLabels : [ 
                        [ name: 'io.openinnovationlabs.jenkins.build.url', value: "${env.BUILD_URL}" ],
                        [ name: 'io.openinnovationlabs.jenkins.build.tag', value: "${env.BUILD_NUMBER}"],
                        [ name: 'io.openinnovationlabs.git.branch', value: "${env.GIT_BRANCH}"],
                        [ name: 'io.openinnovationlabs.git.url', value: "${env.GIT_URL}"],
                        [ name: 'io.openinnovationlabs.git.commit', value: "${env.GIT_COMMIT}"]
                    ]
                ]
            ] 
        ]
    def patchJson = groovy.json.JsonOutput.toJson(patch)

    sh "oc patch bc ${APP_NAME} -p '${patchJson}'"
}
