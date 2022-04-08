pipeline {
    agent {
        label 'contrib'
    }

    environment {
        SLACK_MESSAGE = "${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}|Open>)"
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        CI = credentials("app-jenkins")
        SERVICE_CHECKMARX_URL = credentials("service-checkmarx-url")
        SERVICE_SONAR_URL = credentials("service-sonar-java11-url")
        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_PROXY_HOST = credentials("http-proxy-host")
        SERVICE_PROXY_PORT = credentials("http-proxy-port")
        NOPROXY_HOST = credentials("http_nonProxyHosts")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_REPOSITORY_URL=credentials("service-repository-url")
        JAVA_TOOL_OPTIONS = "-Dhttp.proxyHost=${env.SERVICE_PROXY_HOST} -Dhttp.proxyPort=${env.SERVICE_PROXY_PORT} -Dhttps.proxyHost=${env.SERVICE_PROXY_HOST} -Dhttps.proxyPort=${env.SERVICE_PROXY_PORT} -Dhttp.nonProxyHosts=${env.NOPROXY_HOST}"
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(
            logRotator(
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                numToKeepStr: '100'
            )
        )
    }

//    triggers {
//        cron('45 2 * * *')
//    }

    stages {
          stage('Activate steps') {
            agent none
            steps {
                script {
                    env.DO_MAJ_CONTEXT = 'true'
                    env.DO_TEST = 'true'
                    env.DO_BUILD = 'true'
                    env.DO_PUBLISH = 'true'
                    env.DO_CHECKMARX = 'false'
                }
            }
        }

        stage('Upgrade build context') {
	    when {
                environment(name: 'DO_MAJ_CONTEXT', value: 'true')
            }
            environment {
                NODE_JS_DOWNLOAD_URL="https://rpm.nodesource.com/setup_16.x"
                http_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
                https_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
            }
            steps {
                sh 'sudo yum install -y gcc-c++ make'
                sh 'sudo yum remove -y nodejs'
                sh 'curl -sL https://rpm.nodesource.com/setup_16.x | sudo -E bash -'
          //      sh 'sudo yum install -y nodejs'
                sh 'sudo yum install -y nodejs-16.9.0-1nodesource'
                sh 'node -v'
                sh '/usr/bin/node -v'
                sh 'npm -v'
                sh 'sudo rm /usr/local/bin/node || true'
                sh 'sudo rm /usr/local/bin/npm || true'
                sh 'node -v;npm -v'
            }
        }
        /*
        stage('Build common.') {
             when {
                environment(name: 'DO_TEST', value: 'true')
            }
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
                JAVA_TOOL_OPTIONS=""
            }
            steps {
                parallel(
                    'Common': {
                         sh ''' $MVN_COMMAND verify -Psonar-metrics,vitam -f commons/pom.xml '''
                    }
                )
            }
        }
*/
        stage('Build and tests.') {
             when {
                environment(name: 'DO_TEST', value: 'true')
            }
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
                JAVA_TOOL_OPTIONS=""
            }
            steps {
                parallel(
                    'Build and Test Apis': {
                      //  sh ''' $MVN_COMMAND verify -Psonar-metrics,vitam -f api/pom.xml '''


                sh '''
                    $MVN_COMMAND clean verify -Psonar-metrics,vitam -pl '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express,!ui,!ui/ui-portal,!ui/ui-identity,!ui/ui-frontend,!ui/ui-frontend-common,!ui/ui-ingest,!ui/ui-archive-search ,!ui/ui-referential ' $JAVA_TOOL_OPTIONS
                '''


                    },
                    'Build and Test Ui Frontend Common': {
                        sh 'node -v'
                        sh 'npmrc default'
                        sh ''' $MVN_COMMAND clean verify  -Psonar-metrics,vitam -f ui/ui-frontend-common/pom.xml  '''
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-frontend/pom.xml '''
                    }
                )
            }
        }

        stage('Uis') {
            when {
                environment(name: 'DO_TEST', value: 'true')
            }
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
                JAVA_TOOL_OPTIONS=""
            }
            steps {
                parallel(
                    'Front portal': {
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-portal/pom.xml '''
                    },
                    'Front identity': {
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-identity/pom.xml '''
                    },
                    'Front ingest': {
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-ingest/pom.xml '''
                    },
                    'Front archive-search': {
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-archive-search/pom.xml '''
                    },
                    'Front referential': {
                        sh ''' $MVN_COMMAND verify -Pvitam,sonar-metrics -f ui/ui-referential/pom.xml '''
                    }
                )
            }
        }
/*
        stage('Ui Frontend') {
            steps {
                parallel(
                    'Build ui parent': {
                        sh ''' $MVN_COMMAND install -DskipTests=true -DskipAllFrontendTest -Pvitam -f ui/pom.xml -pl !ui-frontend-common,!ui-frontend,!ui-portal,!ui-identity,!ui-referential '''
                    },
                    'Build and Test Ui Frontend': {
                        sh ''' $MVN_COMMAND install -Pvitam -DskipAllFrontendTest -DskipTests=true -f ui/ui-frontend/pom.xml '''
                    }
                )
            }
        }

        stage('Ui Frontend') {
            steps {
                parallel(
                    'Build ui parent': {
                        sh ''' $MVN_COMMAND install -DskipTests=true -DskipAllFrontendTest -Pvitam -f ui/pom.xml -pl !ui-frontend-common,!ui-frontend,!ui-portal,!ui-identity,!ui-referential '''
                    },
                    'Build and Test Ui Frontend': {
                        sh ''' $MVN_COMMAND install -Pvitam -DskipAllFrontendTest -DskipTests=true -f ui/ui-frontend/pom.xml '''
                    }
                )
            }
        }

        stage('Uis ') {
            steps {
                parallel(
                    'Ui identity': {
                        sh ''' $MVN_COMMAND install -Pvitam -f ui/ui-identity/pom.xml '''
                    },
                    'Ui portal': {
                        sh ''' $MVN_COMMAND install -Pvitam -f ui/ui-portal/pom.xml '''
                    },
                    'Ui referential': {
                        sh ''' $MVN_COMMAND install -Pvitam -f ui/ui-referential/pom.xml  '''
                    }
                )
            }
        }
        */
        /*
        stage('Build and tests.') {
            steps {
                parallel(
                    'Back install and Test': {
                        sh ''' $MVN_COMMAND install -Pvitam -pl !ui,!ui/ui-frontend-common,!ui/ui-frontend,!ui/ui-portal,!ui/ui-identity,!ui/ui-referential '''
                    },
                    'Build and Test Ui Frontend Common': {
                        sh ''' $MVN_COMMAND install -DskipAllFrontendTest -DskipTests=true -Pvitam -f ui/ui-frontend-common/pom.xml  '''
                    }
                )
            }
        }

        */

        stage('Build sources') {
            environment {
                PUPPETEER_DOWNLOAD_HOST = "${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
            }
            when {
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sh 'npmrc default'
                sh '''
                    $MVN_COMMAND deploy -Pvitam,deb,rpm -DskipTests -DskipAllFrontend=true -DskipAllFrontendTests=true -Dlicense.skip=true -pl '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express' $JAVA_TOOL_OPTIONS
                '''
            }
        }

        stage('Build COTS') {
            environment {
                http_proxy = "http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
                https_proxy = "http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
            }
            when {
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sh 'npmrc internet'
                dir('cots/') {
                    sh '''
                        $MVN_COMMAND deploy -Pvitam,deb,rpm -DskipTests -Dlicense.skip=true $JAVA_TOOL_OPTIONS
                    '''
                }
            }
        }

        stage('Get publishing scripts') {
            when {
                environment(name: 'DO_PUBLISH', value: 'true')
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: 'oshimae']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'vitam-build.git']],
                    submoduleCfg: [],
                    userRemoteConfigs: [[credentialsId: 'app-jenkins', url: "$SERVICE_GIT_URL"]]
                ])
            }
        }

        stage('Publish rpm and deb') {
            when {
                environment(name: 'DO_PUBLISH', value: 'true')
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sshagent (credentials: ['jenkins_sftp_to_repository']) {
                    sh 'vitam-build.git/push_vitamui_repo.sh contrib $SERVICE_REPO_SSHURL rpm'
                    sh 'vitam-build.git/push_vitamui_repo.sh contrib $SERVICE_REPO_SSHURL deb'
                }
            }
        }

        stage('Update symlink') {
            when {
                anyOf {
                    branch 'develop*'
                    branch 'master_*'
                    tag pattern: "^[1-9]+(\\.rc)?(\\.[0-9]+)?\\.[0-9]+(-.*)?", comparator: 'REGEXP'
                }
                environment(name: 'DO_PUBLISH', value: 'true')
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sshagent (credentials: ['jenkins_sftp_to_repository']) {
                    sh 'vitam-build.git/push_symlink_repo.sh contrib $SERVICE_REPO_SSHURL'
                }
            }
        }

        stage('Checkmarx analysis') {
            when {
                anyOf {
                    branch 'develop*'
                    branch 'master_*'
                    branch 'master'
                    tag pattern: "^[1-9]+(\\.rc)?(\\.[0-9]+)?\\.[0-9]+(-.*)?", comparator: 'REGEXP'
                }
                environment(name: 'DO_CHECKMARX', value: 'true')
            }
            environment {
                JAVA_TOOL_OPTIONS = ''
            }
            steps {
                dir('vitam-build.git') {
                    deleteDir()
                }
                sh 'mkdir -p target'
                sh 'mkdir -p logs'
                // KWA : Visibly, backslash escape hell. \\ => \ in groovy string.
                sh '/opt/CxConsole/runCxConsole.sh scan --verbose -Log "${PWD}/logs/cxconsole.log" -CxServer "$SERVICE_CHECKMARX_URL" -CxUser "VITAM openLDAP\\\\$CI_USR" -CxPassword \\"$CI_PSW\\" -ProjectName "CxServer\\SP\\Vitam\\Users\\vitam-ui $GIT_BRANCH" -LocationType folder -locationPath "${PWD}/"  -Preset "Default 2014" -LocationPathExclude "cots,deployment,deploymentByVitam,docs,integration-tests,tools,node,node_modules,dist,target" -LocationFilesExclude "*.rpm,*.pdf" -ForceScan -ReportPDF "${PWD}/target/checkmarx-report.pdf"'
            }
            post {
                success {
                    archiveArtifacts (
                        artifacts: 'target/checkmarx-report.pdf',
                        fingerprint: true
                    )
                }
                failure {
                    archiveArtifacts (
                        artifacts: 'logs/cxconsole.log',
                        fingerprint: true
                    )
                }
            }
        }
    }
}
