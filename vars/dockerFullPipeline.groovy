def call(Map config = [:]) {
    // Variables set kar rahe hain
    def repoUrl = config.repoUrl ?: error("Repository URL is required!")
    def imageName = config.imageName ?: error("Image Name is required!")
    def credsId = config.credsId ?: 'docker-hub-credentials'
    def branchName = config.branch ?: 'main'

    pipeline {
        agent any

        environment {
            IMAGE_NAME = "${imageName}"
            IMAGE_TAG  = "latest"
        }

        stages {
            // --- STAGE 1 ---
            stage('Checkout Code') {
                steps {
                    echo "Cloning Branch: ${branchName}"
                    git branch: branchName, url: repoUrl
                }
            } // <--- Yeh bracket zaroori hai (End of Stage 1)

            // --- STAGE 2 ---
            stage('Build Docker Image') {
                steps {
                    echo "Building Image..."
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            } // <--- Yeh bracket zaroori hai (End of Stage 2)

            // --- STAGE 3 ---
            stage('DockerHub Login & Push') {  // (Line 33 was failing here)
                steps {
                    script {
                        echo "Logging into DockerHub..."
                        withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            sh """
                                echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                                docker push ${IMAGE_NAME}:${IMAGE_TAG}
                            """
                        }
                    }
                }
            } // <--- Yeh bracket zaroori hai (End of Stage 3)

            // --- STAGE 4 ---
            stage('Deploy') {
                steps {
                    echo "Deploying..."
                    // Compose down/up command
                    sh '''
                        docker-compose down || true
                        docker-compose up -d
                    '''
                }
            } // <--- Yeh bracket zaroori hai (End of Stage 4)

        } // <--- End of 'stages'

        post {
            success {
                echo '✅ Pipeline Success!'
            }
            failure {
                echo '❌ Pipeline Failed.'
            }
        } // <--- End of 'post'

    } // <--- End of 'pipeline'
} // <--- End of 'call' function
