pipeline {
    agent any

    environment {
        IMAGE_NAME = 'xaiht-kyber'
        IMAGE_TAG = 'latest'
        CONTAINER_NAME = 'xaiht-kyber-app'
        DOCKER_APP_PORT = '8080'
        K8S_NAMESPACE = 'default'
        K8S_DEPLOYMENT = 'xaiht-kyber-deployment'
        K8S_APP_LABEL = 'app=xaiht-kyber'
        K8S_ROLLOUT_TIMEOUT_SECONDS = '360'
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
                echo 'Applying the Kubernetes manifest and polling deployment readiness.'
                bat '''
                    kubectl apply -f kubernetes-deployment.yaml
                    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
                      "$ErrorActionPreference = 'Stop';" ^
                      "$namespace = $env:K8S_NAMESPACE;" ^
                      "$deployment = $env:K8S_DEPLOYMENT;" ^
                      "$appLabel = $env:K8S_APP_LABEL;" ^
                      "$timeoutSeconds = [int]$env:K8S_ROLLOUT_TIMEOUT_SECONDS;" ^
                      "$deadline = (Get-Date).AddSeconds($timeoutSeconds);" ^
                      "$ready = $false;" ^
                      "Write-Host ('Waiting up to {0} seconds for deployment {1} in namespace {2}.' -f $timeoutSeconds, $deployment, $namespace);" ^
                      "while ((Get-Date) -lt $deadline) {" ^
                      "  try {" ^
                      "    $deploymentJson = kubectl get deployment $deployment -n $namespace -o json | ConvertFrom-Json;" ^
                      "    $desired = [int]($deploymentJson.spec.replicas);" ^
                      "    $updated = [int]($deploymentJson.status.updatedReplicas);" ^
                      "    $available = [int]($deploymentJson.status.availableReplicas);" ^
                      "    $observedGeneration = [int]($deploymentJson.status.observedGeneration);" ^
                      "    $generation = [int]($deploymentJson.metadata.generation);" ^
                      "    Write-Host ('Deployment state: observedGeneration={0}/{1} updated={2}/{3} available={4}/{3}' -f $observedGeneration, $generation, $updated, $desired, $available);" ^
                      "    if ($observedGeneration -ge $generation -and $updated -ge $desired -and $available -ge $desired) { $ready = $true; break }" ^
                      "  } catch {" ^
                      "    Write-Host ('Transient kubectl error while polling deployment status: {0}' -f $_.Exception.Message);" ^
                      "  }" ^
                      "  Start-Sleep -Seconds 5;" ^
                      "}" ^
                      "if (-not $ready) {" ^
                      "  Write-Host 'Deployment did not become ready before timeout. Capturing diagnostics.';" ^
                      "  kubectl get deployment $deployment -n $namespace -o wide;" ^
                      "  kubectl get pods -n $namespace -l $appLabel -o wide;" ^
                      "  kubectl describe deployment $deployment -n $namespace;" ^
                      "  kubectl describe pods -n $namespace -l $appLabel;" ^
                      "  try { kubectl logs -n $namespace -l $appLabel --all-containers=true --tail=200 } catch { Write-Host 'Container logs were not available.' }" ^
                      "  throw 'Kubernetes deployment rollout did not complete before timeout.';" ^
                      "}"
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