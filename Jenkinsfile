pipeline {
  environment {
    devRegistry = 'ghcr.io/datakaveri/di-dev'
    deplRegistry = 'ghcr.io/datakaveri/di-depl'
    registryUri = 'https://ghcr.io'
    registryCredential = 'datakaveri-ghcr'
    GIT_HASH = GIT_COMMIT.take(7)
  }
  agent { 
    node {
      label 'slave1' 
    }
  }
  stages{
    stage('Conditional Execution') {
      when {
        anyOf {
          triggeredBy 'UserIdCause'
          expression {
            def comment = env.ghprbCommentBody?.trim()
            return comment && comment.toLowerCase() != "null"
          }
          changeset "docker/**"
          changeset "docs/**"
          changeset "pom.xml"
          changeset "src/main/**"
        }
      }
      stages {
        stage('Trivy Code Scan (Dependencies)') {
          steps {
            script {
              sh '''
                trivy fs --scanners vuln,secret,misconfig --output trivy-fs-report.txt .
              '''
            }
          }
        }
        stage('Building images') {
          steps{
            script {
              echo 'Pulled - ' + env.GIT_BRANCH
              devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
              deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
            }
          }
        }
        stage('Trivy Docker Image Scan and Report') {
          steps {
            script {
              sh "trivy image --output trivy-dev-image-report.txt ${devImage.imageName()}"
              sh "trivy image --output trivy-depl-image-report.txt ${deplImage.imageName()}"
            }
          }
          post {
            always {
              archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
              publishHTML(target: [
                allowMissing: true,
                keepAll: true,
                reportDir: '.',
                reportFiles: 'trivy-fs-report.txt, trivy-dev-image-report.txt, trivy-depl-image-report.txt',
                reportName: 'Trivy Reports'
              ])
            }
          }
        }
        stage('Unit Tests and Code Coverage Test'){
          steps{
            script {
              sh 'sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java'
              sh 'mkdir -p configs'
              sh 'cp /home/ubuntu/configs/di-config-test.json ./configs/config-test.json'
              sh 'cp /home/ubuntu/configs/di-config-test.json ./configs/config-dev.json'
              sh 'cp /home/ubuntu/configs/keystore-di.jks ./configs/keystore.jks'
              sh 'mvn clean test checkstyle:checkstyle pmd:pmd'
            }
            xunit (
              thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
              tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
            )
            jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern: 'iudx/data/ingestion/server/apiserver/*.class,**/*VertxEBProxy.class,**/Constants.class,**/*VertxProxyHandler.class,**/*Verticle.class,iudx/data/ingestion/server/deploy/*.class,iudx/data/ingestion/server/metering/MeteringService.class,iudx/data/ingestion/server/databroker/DataBrokerService.class'
          }
          post{
          always {
                                recordIssues(
                                  enabledForFailure: true,
                                  skipBlames: true,
                                  qualityGates: [[threshold:3, type: 'TOTAL', unstable: false]],

                                  tool: checkStyle(pattern: 'target/checkstyle-result.xml')
                                )
                                recordIssues(
                                  enabledForFailure: true,
                                  skipBlames: true,
                                  qualityGates: [[threshold:1, type: 'TOTAL', unstable: false]],
                                  tool: pmdParser(pattern: 'target/pmd.xml')
                                )
                              }
            failure{
              xunit (
                thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
                tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
              )
              error "Test failure. Stopping pipeline execution!"
            }
            cleanup{
              script{
                sh 'sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java'
                sh 'sudo rm -rf target/'
              }
            }
          }
        }

        stage('Start Data-Ingestion Server for Integration testing'){
          steps{
            script{
                sh 'scp src/test/resources/IUDX-Data-Ingestion-Server-APIs-V5.5.0.postman_collection.json jenkins@jenkins-master:/var/lib/jenkins/iudx/di/Newman/'
                sh 'docker compose -f docker-compose.test.yml up -d integTest'
                sh 'sleep 45'
            }
          }
          post{
            failure{
              script{
                sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
              }
            }
          }
        }

        stage('Integration Tests & OWASP ZAP pen test'){
          steps{
            node('built-in') {
              script{
                startZap ([host: 'localhost', port: 8090, zapHome: '/var/lib/jenkins/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/OWASP_ZAP/ZAP_2.11.0'])
                  sh 'curl http://127.0.0.1:8090/JSON/pscan/action/disableScanners/?ids=10096'
                  sh 'HTTP_PROXY=\'127.0.0.1:8090\' newman run /var/lib/jenkins/iudx/di/Newman/IUDX-Data-Ingestion-Server-APIs-V5.5.0.postman_collection.json -e /home/ubuntu/configs/di-postman-env.json -n 2 --insecure -r htmlextra --reporter-htmlextra-export /var/lib/jenkins/iudx/di/Newman/report/report.html --reporter-htmlextra-skipSensitiveData'
                runZapAttack()
              }
            }
          }
          post{
            always{
              node('built-in') {
                script{
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '/var/lib/jenkins/iudx/di/Newman/report/', reportFiles: 'report.html', reportName: 'HTML Report', reportTitles: '', reportName: 'Integration Test Report'])
                  archiveZap failHighAlerts: 1, failMediumAlerts: 1, failLowAlerts: 1
                }  
              }
            }
            failure{
              error "Test failure. Stopping pipeline execution!"
            }
            cleanup{
              script{
                sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
              }
            }
          }
        }
        stage('Continuous Deployment') {
          when {
            expression {
                return env.GIT_BRANCH == 'origin/main';
              }
          }
          stages {
            stage('Push Images') {
              steps {
                script {
                  docker.withRegistry( registryUri, registryCredential ) {
                    devImage.push("6.0.0-alpha-${env.GIT_HASH}")
                    deplImage.push("6.0.0-alpha-${env.GIT_HASH}")
                  }
                }
              }
            }
            stage('Docker Swarm deployment') {
              steps {
                script {
                  sh "ssh azureuser@docker-swarm 'docker service update di_di --image ghcr.io/datakaveri/di-depl:6.0.0-alpha-${env.GIT_HASH}'"
                  sh 'sleep 10'
                }
              }
              post{
                failure{
                  error "Failed to deploy image in Docker Swarm"
                }
              }          
            }
            stage('Integration test on swarm deployment') {
              steps {
                node('built-in') {
                  script{
                    sh 'newman run /var/lib/jenkins/iudx/di/Newman/IUDX-Data-Ingestion-Server-APIs-V5.5.0.postman_collection.json -e /home/ubuntu/configs/cd/di-postman-env.json --insecure -r htmlextra --reporter-htmlextra-export /var/lib/jenkins/iudx/di/Newman/report/cd-report.html --reporter-htmlextra-skipSensitiveData'
                  }
                }
              }
              post{
                always{
                  node('built-in') {
                    script{
                      publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '/var/lib/jenkins/iudx/di/Newman/report/', reportFiles: 'cd-report.html', reportTitles: '', reportName: 'Docker-Swarm Integration Test Report'])
                    }
                  }
                }
                failure{
                  error "Test failure. Stopping pipeline execution!"
                }
              }
            }
          }
        }
      }
    }
  }
  post{
    failure{
      script{
        if (env.GIT_BRANCH == 'origin/main')
        emailext recipientProviders: [buildUser(), developers()], to: '$RS_RECIPIENTS, $DEFAULT_RECIPIENTS', subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!', body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
      }
    }
  }
}