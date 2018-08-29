# Shared Jenkins Library

This library provides a few common methods used during Jenkins pipelines.

## Usage

This project can be imported into a Jenkins pipeline with a Jenkinsfile. Here is a sample pipeline that uses a method in this repo

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