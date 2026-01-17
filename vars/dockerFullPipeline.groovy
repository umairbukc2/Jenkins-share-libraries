def call(Map config = [:]) {
    // Arguments receive kar rahe hain
    def repoUrl = config.repoUrl ?: error("Repo URL missing")
    def imageName = config.imageName ?: error("Image Name missing")
    def credsId = config.credsId ?: 'docker-hub-credentials'
    def branchName = config.branch ?: 'main'

    pipeline {
        agent any

        environment {
            IMAGE_NAME = "${imageName}"
            IMAGE_TAG  = "latest"
        }

        stages {
            // Stage 1: Code Clone
            stage('Checkout Code') {
                steps {
                    echo "Cloning Repo..."
                    git branch: branchName, url: repoUrl
                }
            }

            // Stage 2: Build Image (Yeh raha wo stage jo aapne maanga)
            stage('Build Docker Image') {
                steps {
                    echo "🛠️ Building Docker Image..."
                    // Yeh command image create karegi
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }

            // Stage 3: Push to DockerHub
            stage('Push Image') {
                steps {
                    script {
                        echo "🚀 Pushing to DockerHub..."
                        withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                            sh """
                                echo "\$PASS" | docker login -u "\$USER" --password-stdin
                                docker push ${IMAGE_NAME}:${IMAGE_TAG}
                            """
                        }
                    }
                }
            }

            // Stage 4: Deploy
            stage('Deploy') {
                steps {
                    echo "🔥 Deploying Container..."
                    sh """
                        docker-compose down || true
                        docker-compose up -d
                    """
                }
            }
        }
    }
}
