def call( String appName, String targetImageName, String sourceProjectName, String targetProjectName, String deployTag){
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
