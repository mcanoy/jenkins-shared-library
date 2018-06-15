def call(String appName, String projectName ){     
    openshift.withCluster () {
        echo "Verifying deployment with project " + projectName + " of app " + appName + " 10 second wait"
        sleep 10 // give the deployment a few seconds. Will never complete that fast
        openshift.withProject( projectName ){
            def latestDeploymentVersion = openshift.selector('dc', appName ).object().status.latestVersion
            echo "Latest version " + latestDeploymentVersion
            def rc = openshift.selector('rc', "${appName}-${latestDeploymentVersion}")
            
            rc.untilEach(1){
                def rcMap = it.object()
                echo "Saw " + rcMap.status.replicas.equals(rcMap.status.readyReplicas)
                return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
            }
        }
    }
    echo "Verification complete"
}
