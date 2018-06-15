def call(String sourceImage, String targetImageName, String deployImageTag){
    openshift.withCluster () {
        try {
            echo "Tagging ${targetImageName}:${deployImageTag} ${targetImageName}:${deployImageTag}-previous"
            openshift.tag( "${targetImageName}:{$deployImageTag} ${targetImageName}:${deployImageTag}-previous" )
        } catch (Exception ex) {
            echo 'Failed to tag for previous'
        }
        echo "Tagging ${sourceImage} ${targetImageName}:${deployImageTag}-latest"
        openshift.tag( "${sourceImage} ${targetImageName}:${deployImageTag}-latest" )
        echo "Tagging ${sourceImage} ${targetImageName}:${deployImageTag}"
        openshift.tag( "${sourceImage} ${targetImageName}:${deployImageTag}" )
    }
}
