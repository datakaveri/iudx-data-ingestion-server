pipeline {
  environment {
    devRegistry = 'ghcr.io/datakaveri/di-dev'
    deplRegistry = 'ghcr.io/datakaveri/di-depl'
    testRegistry = 'ghcr.io/datakaveri/di-test:latest'
    registryUri = 'https://ghcr.io'
    registryCredential = 'datakaveri-ghcr'
    GIT_HASH = GIT_COMMIT.take(7)
  }
  agent { 
    node {
      label 'slave1' 
    }
  }
  stages {

    stage('Building images') {
      steps{
        script {
          echo 'Pulled - ' + env.GIT_BRANCH
          devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
          deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
          testImage = docker.build( testRegistry, "-f ./docker/test.dockerfile .")
        }
      }
    }

    stage('Unit Tests and Code Coverage Test'){
      steps{
        script{
          sh 'docker-compose -f docker-compose.test.yml up test'
        }
        xunit (
          thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
          tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
        )
        jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern: 'iudx/data/ingestion/server/apiserver/*.class,**/*VertxEBProxy.class,**/Constants.class,**/*VertxProxyHandler.class,**/*Verticle.class,iudx/data/ingestion/server/deploy/*.class'
      }
      post{
        failure{
          script{
            sh 'docker-compose down --remove-orphans'
          }
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'sudo rm -rf target/'
          }
        }
      }
    }

    stage('Start Data-Ingestion Server for Integration testing'){
      steps{
        script{
            sh 'scp src/test/resources/IUDX_Data_Ingestion_Server.postman_collection.json jenkins@jenkins-master:/var/lib/jenkins/iudx/di/Newman/'
            sh 'docker-compose -f docker-compose.test.yml up -d integTest'
            sh 'sleep 45'
        }
      }
    }

    stage('Integration Tests & OWASP ZAP pen test'){
      steps{
        node('master') {
          script{
            startZap ([host: 'localhost', port: 8090, zapHome: '/var/lib/jenkins/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/OWASP_ZAP/ZAP_2.11.0'])
              sh 'curl http://127.0.0.1:8090/JSON/pscan/action/disableScanners/?ids=10096'
              sh 'HTTP_PROXY=\'127.0.0.1:8090\' newman run /var/lib/jenkins/iudx/di/Newman/IUDX_Data_Ingestion_Server.postman_collection.json -e /home/ubuntu/configs/di-postman-env.json --insecure -r htmlextra --reporter-htmlextra-export /var/lib/jenkins/iudx/di/Newman/report/report.html --reporter-htmlextra-skipSensitiveData'
            runZapAttack()
          }
        }
      }
      post{
        always{
          node('master') {
            script{
               archiveZap failHighAlerts: 1, failMediumAlerts: 1, failLowAlerts: 1
            }  
            publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '/var/lib/jenkins/iudx/di/Newman/report/', reportFiles: 'report.html', reportName: 'HTML Report', reportTitles: '', reportName: 'Integration Test Report'])
          }
        }
        failure{
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'docker-compose -f docker-compose.test.yml down --remove-orphans'
          }
        }
      }
    }

    stage('Push Image') {
      when{
        expression {
          return env.GIT_BRANCH == 'origin/main';
        }
      }
      steps{
        script {
          docker.withRegistry( registryUri, registryCredential ) {
            devImage.push("4.0-alpha-${env.GIT_HASH}")
            deplImage.push("4.0-alpha-${env.GIT_HASH}")
          }
        }
      }
    }
  }
}