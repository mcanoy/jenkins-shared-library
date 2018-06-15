// This is a simple method to process a canary deployment. It is based on routing to 2 services and 
// a 100 point scale. So, the request is for 10% of traffic to go to the alternate service then that 
// service's weight will be set to 10 and the 'to' service will be set to 90
// At 100 the services will be flipped (ie. 'to' becomes alternate)
def call(int weight, String appName, String namespace) {
    def serviceNames = sh(script: "oc get route $appName -o jsonpath='{.spec.to.name}||{.spec.alternateBackends[0].name}' -n ${namespace}", returnStdout: true)
    def services = serviceNames.tokenize('||')

    def toName, toWeight, altName, altWeight

    //If requesting all traffic to post to the alternate service. Then flip the service to be the main service
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

    def patchJson = groovy.json.JsonOutput.JsonOutput.toJson(patch)
    sh "oc patch route ${appName} -p '${patchJson}'  -n ${namespace}"

}
