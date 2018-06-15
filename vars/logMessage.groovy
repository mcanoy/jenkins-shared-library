def call(String name = 'no echo message provided') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Message: ${name}"
}
