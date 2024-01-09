pipeline {
    agent any

    environment {
        // Set your Kubernetes cluster credentials
        KUBE_CONFIG = credentials('c7bc4fb6-be60-4879-86aa-b1362598deeb')
        KUBE_NAMESPACE = 'jenkins'
    }

    stages {
      stage('Checkout') {
            steps {
                // Assuming you have your code in a Git repository
                checkout scm
            }
        }
    stage('Build Docker Image') {
            steps {
                script {
                    // Execute your .sh script to build Docker image
                    sh './ClimateChecker/docker-build.sh'
                }
            }
        }

      
        stage('Build and Deploy') {
            steps {
                script {
                    // Define the pod YAML configuration
                    def podConfig = """
                    apiVersion: apps/v1
kind: Deployment
metadata:
  name: ontdekstation-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ontdekstation-server
  template:
    metadata:
      labels:
        app: ontdekstation-server
    spec:
      containers:
      - name: ontdekstation-server
        image: ontdekstation-server:latest
        ports:
        - containerPort: 8082
---
apiVersion: v1
kind: Service
metadata:
  name: ontdekstation-service
spec:
  selector:
    app: ontdekstation-server
  ports:
    - protocol: TCP
      port: 8082
      targetPort: 8082
  type: LoadBalancer
"""
                    
                    // Save the pod configuration to a file
                    writeFile file: 'ontdekstation-server.yaml', text: podConfig

                    // Use kubectl to apply the pod configuration
                    sh('kubectl --kubeconfig=${KUBE_CONFIG} apply -f ontdekstation-server.yaml -n ${KUBE_NAMESPACE}')
                }
            }
        }
    }
}
