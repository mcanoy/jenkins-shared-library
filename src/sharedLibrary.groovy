// this type of thing should go in a shared library in the future
// https://jenkins.io/doc/book/pipeline/shared-libraries/

import groovy.json.JsonOutput;

def canaryDeployment(int weight, String appName, String namespace) {
    def serviceNames = sh(script: "oc get route $appName -o jsonpath='{.spec.to.name}||{.spec.alternateBackends[0].name}' -n ${namespace}", returnStdout: true)
    def services = serviceNames.tokenize('||')

    def toName, toWeight, altName, altWeight

    if(weight == 100) {
        toName = services.get(1)
        toWeight = 100;
        altName = services.get(0);
        altWeight = 0;
    } else {
        toName = services.get(0);
        toWeight = 100 - weight;
        altWeight = weight;
        altName = services.get(1)
    }

    def patch = [
        spec: [
            to: [
                 name: "$toName",
                 weight: toWeight
            ],
            alternateBackends: [
                [
                    name: "$altName",
                    weight: altWeight
                ]
                
            ]
        ]
    ]

    def patchJson = JsonOutput.toJson(patch)
    sh "oc patch route ${appName} -p '${patchJson}'  -n ${namespace}"

}

def patchBuildConfigOutputLabels(env) {

    // needed because https://github.com/openshift/jenkins/issues/574
    // TODO maybe move to jenkins client? I dunno, that client feels flaky...
    def jenkinsHost = sh(script: 'oc get route jenkins --template={{.spec.host}}', returnStdout: true)

    def patch = [
            spec: [
                output : [
                    imageLabels : [ 
                        [ name: 'io.openinnovationlabs.jenkins.build.url', value: "${env.RUN_DISPLAY_URL}".replace('unconfigured-jenkins-location', jenkinsHost) ],
                        [ name: 'io.openinnovationlabs.jenkins.build.tag', value: "${env.BUILD_NUMBER}"],
                        [ name: 'io.openinnovationlabs.git.branch', value: "${env.GIT_BRANCH}"],
                        [ name: 'io.openinnovationlabs.git.url', value: "${env.GIT_URL}"],
                        [ name: 'io.openinnovationlabs.git.commit', value: "${env.GIT_COMMIT}"]
                    ]
                ]
            ] 
        ]
    def patchJson = JsonOutput.toJson(patch)

    // TODO maybe move to jenkins client? I dunno, that client feels flaky...
    sh "oc patch bc ${APP_NAME} -p '${patchJson}'"
}

/*
    There seems to be some instability with this approach, even though it's exactly the same impl from the old plugin
    The logging is crappy in the client, so very hard to debug what the failure is at the moment.
*/
def verifyDeployment(String appName, String projectName ){     
    openshift.withCluster () {
        echo "Verifying deployment with project " + projectName + " of app " + appName + " 10 second wait"
        sleep 10 // give the deployment a few seconds. Will never complete that fast
        openshift.withProject( projectName ){
            def latestDeploymentVersion = openshift.selector('dc', appName ).object().status.latestVersion
            echo "Latest version " + latestDeploymentVersion
            def rc = openshift.selector('rc', "${appName}-${latestDeploymentVersion}")
            
            echo "rc"
            echo rc.toString()
            rc.untilEach(1){
                def rcMap = it.object()
                echo "Saw " + rcMap.status.replicas.equals(rcMap.status.readyReplicas)
                return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
            }
        }
    }
    echo "Verification complete"
}

def promoteImageWithinCluster( String appName, String targetImageName, String sourceProjectName, String targetProjectName, String deployTag){
    openshift.withCluster () {
        try {
            echo "Tagging ${targetProjectName}/${targetImageName}:latest ${targetProjectName}/${targetImageName}:previous-${deployTag}"
            openshift.tag( "${targetProjectName}/${targetImageName}:latest ${targetProjectName}/${targetImageName}:previous-${deployTag}" )
        } catch (Exception ex) {
            echo 'Failed to tag for previous'
        }
        echo "Tagging ${sourceProjectName}/${appName}:latest ${targetProjectName}/${targetImageName}:latest-${deployTag}"
        openshift.tag( "${sourceProjectName}/${appName}:latest ${targetProjectName}/${targetImageName}:latest-${deployTag}" )
        echo "Tagging ${sourceProjectName}/${appName}:latest ${targetProjectName}/${targetImageName}:${deployTag}"
        openshift.tag( "${sourceProjectName}/${appName}:latest ${targetProjectName}/${targetImageName}:${deployTag}" )
    }
}


def applyAnsibleInventory( String tag ){
    sh """
        cd .openshift-applier
        ansible-galaxy install -r requirements.yml --roles-path=roles
        ansible-playbook -i inventory/ apply.yml -e filter_tags=${tag}
    """
}
return this;