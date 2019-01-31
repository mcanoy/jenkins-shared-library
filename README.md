# Shared Jenkins Library

This library provides a few common methods used during Jenkins pipelines.

## Usage

This project can be imported into a Jenkins pipeline with a Jenkinsfile. Here is a sample pipeline that uses a method in this repo or can be made available by configuring a global shared library in jenkins. See [here](https://github.com/mcanoy/labs-ci-cd/tree/config-jenkins/s2i-config/jenkins-master) for an example of automating the global shared library.

```
pipeline {
    agent { label 'jenkins-slave-mvn' }

    stages {
        stage('Import Library') {
            steps {
                library identifier: 'shared-library@master',
                    retriever: modernSCM([$class: 'GitSCMSource',
                    credentialsId: 'openshift-project-git-ssh-secret',
                    remote: 'git@gitlab.com:ehealth-lab/shared-jenkins-library.git',
                ])
            }
        stage('Container Build'){
            steps {
                patchBuildConfigOutputLabels(env)

                script{
                    openshift.withCluster () {
                        openshift.startBuild( "${APP_NAME} -w" )
                    }
                }
            }
        }
    }
}
```

## Available Library Methods

### applyAnsibleInventory

This method will run an ansible playbook in a subdirectory `.openshift-applier`. A string can be passed that contains the filter tags to apply

```
applyAnsibleInventory('dev')
```

### canaryDeployment

A simplified canary deployment method for OCP. It assumes a route that has two services and weights on a 100 point scale. To direct traffic 10% of the alternate backend set the weight to 10. The 'to' service will be set to 90. If you set the weight to 100 the services will be flipped (ie. the 'to' becomes the alternate) 

```
canaryDeployment(20, 'awesome-app', 'prod-project')
```

### logMessage

Essentially the same as calling echo except that it puts the text 'Message: ' in front of the message

```
logMessage('The buns are ready')
```

### patchBuildConfigOutputLabels

Patches the labels that end up on the container image with information from the build. Currently that includes the jenkins build, url and number as well as git's url, branch and commit. (If using a declarative pipeline the git values are already set. If using scm checkout you need add those values from scm checkout)

```
patchBuildConfigOutputLabels(env)
```

### promoteImageWithinCluster

Promotes an image in a cluster through tagging. Allows images to be deployed in different projects. It takes a promotion image and tags it to the target. It also tags any current image with the same tag, appending `-previous` to the tag name.

```
promoteImageWithinCluster('bank-service:latest', 'dev-env:bank-service', 'deployed')
```

### slackBuildResult

Sends a message through slack with the result of the build via the slackSend method to the default jenkins slack channel. *requires slack plugin

```
    post {
        always {
            slackBuildResult(currentBuild.currentResult)
        }
    }
```

### verifyDeployment

Checks an OCP deployment to see if that deployment is complete

```
verifyDeployment('bank-service', 'dev-env')
```




