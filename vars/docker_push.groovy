def call(Map config = [:]) {
    def imageName = config.imageName ?: error("notes-app")
    def imageTag = config.imageTag ?: 'latest'
    def credentials = config.credentials ?: 'docker-hub-750'
    
    echo "Pushing Docker image: ${imageName}:${imageTag}"
    
    withCredentials([usernamePassword(
        credentialsId: credentials,
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )]) {
        sh """
            echo "\$DOCKER_PASSWORD" | docker login -u "\$DOCKER_USERNAME" --password-stdin
            docker push ${imageName}:${imageTag}
            docker push ${imageName}:latest
        """
    }
}
