def call(String buildResult) {
  if ( buildResult == "SUCCESS" ) {
    slackSend color: "good", message: ":success: Job: ${env.JOB_NAME} ${env.BUILD_NUMBER} was successful"
  }
  else if( buildResult == "FAILURE" ) { 
    slackSend color: "danger", message: ":epic_fail: Job: ${env.JOB_NAME} ${env.BUILD_NUMBER} was failed"
  }
  else if( buildResult == "UNSTABLE" ) { 
    slackSend color: "warning", message: ":fry: Job: ${env.JOB_NAME} ${env.BUILD_NUMBER} was unstable"
  }
  else {
    slackSend color: "danger", message: ":fry: Job: ${env.JOB_NAME} ${env.BUILD_NUMBER} its result was unclear"	
  }
}
