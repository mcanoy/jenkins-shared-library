def call( String tag, String... extraArgs ){

    String extras = "";
    extraArgs.each { extras = extras + " -e " + it }

    sh """
        cd .openshift-applier
        ansible-galaxy install -r requirements.yml --roles-path=roles
        ansible-playbook -i inventory/ apply.yml -e filter_tags=${tag} ${extras}
    """
}
