pipeline {
    agent any

    environment {
        IMAGE_NAME = 'xaiht-kyber'
        IMAGE_TAG = 'latest'
        CONTAINER_NAME = 'xaiht-kyber-app'
        DOCKER_APP_PORT = '8080'
        K8S_NAMESPACE = 'default'
        K8S_DEPLOYMENT = 'xaiht-kyber-deployment'
    }

    options {
        timestamps()
    }

    stages {
        stage('Clean Kubernetes Resources') {
            steps {
                echo 'Removing previous Kubernetes deployment and service if they exist.'
                bat '''
                    kubectl delete -f kubernetes-deployment.yaml --ignore-not-found=true
                '''
            }
        }

        stage('Clean Docker Runtime') {
            steps {
                echo 'Stopping and removing the previous local Docker container and image.'
                bat '''
                    docker rm -f %CONTAINER_NAME% 2>nul
                    for /f "usebackq delims=" %%i in (`docker images %IMAGE_NAME% --format "{{.Repository}}:{{.Tag}}"`) do docker image rm -f %%i
                    exit /b 0
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building the Docker image for the application.'
                bat '''
                    docker build -t %IMAGE_NAME%:%IMAGE_TAG% .
                '''
            }
        }

        stage('Redeploy Local Docker App') {
            steps {
                echo 'Running the refreshed image locally with Docker.'
                bat '''
                    docker run -d --name %CONTAINER_NAME% -p %DOCKER_APP_PORT%:8080 %IMAGE_NAME%:%IMAGE_TAG%
                '''
            }
        }

        stage('Redeploy Kubernetes App') {
            steps {
                echo 'Applying the Kubernetes manifest and waiting for rollout.'
                bat '''
                    kubectl apply -f kubernetes-deployment.yaml
                    kubectl rollout status deployment/%K8S_DEPLOYMENT% -n %K8S_NAMESPACE% --timeout=180s
                '''
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Docker image rebuilt and application redeployed to Docker and Kubernetes.'
        }
        failure {
            echo 'Pipeline failed. Check the console output for the failing Docker or Kubernetes step.'
        }
    }
}