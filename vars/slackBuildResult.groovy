def call(String buildResult) {

  def job = "$env.APP_NAME"

  if(job == null) {
    job = "${env.JOB_NAME}"
  }

  if ( buildResult == "SUCCESS" ) {
    slackSend color: "good", message: ":success: Job: ${job} <${env.BUILD_URL}|${env.BUILD_NUMBER}> was successful"
  }
  else if( buildResult == "FAILURE" ) { 
    slackSend color: "danger", message: ":epic_fail: Job: ${job} <${env.BUILD_URL}|${env.BUILD_NUMBER}> failed"
  }
  else if( buildResult == "UNSTABLE" ) { 
    slackSend color: "warning", message: ":fry: Job: ${job} <${env.BUILD_URL}|${env.BUILD_NUMBER}> was unstable"
  }
  else {
    slackSend color: "danger", message: ":fry: Job: ${job} <${env.BUILD_URL}|${env.BUILD_NUMBER}>. Its result was unclear. Maybe aborted?"
  }
}
