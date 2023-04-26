pipeline {

    agent {
        label 'java17'
    }


    environment {
        SLACK_MESSAGE = "${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}|Open>)"
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        M2_REPO = "${HOME}/.m2"
        CI = credentials("app-jenkins")

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
        timeout(time: 4, unit: 'HOURS')
        disableConcurrentBuilds()
        buildDiscarder(
            logRotator(
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                numToKeepStr: '100'
            )
        )
    }

    triggers {
        cron( env.BRANCH_NAME == 'develop' ? '00 20 * * *' : '')
    }

    stages {
        stage('Set variables for automatic run') {
            agent none
            steps {
                script {
                    env.DO_MAJ_CONTEXT = 'true'
                    env.DO_TEST = 'true'
                    env.DO_BUILD = 'true'
                    env.DO_PUBLISH = 'true'
                    env.DO_CHECKMARX = 'false'
                    env.DO_CHECKMARX_SCA = 'true'
                }
            }
        }

        stage('Ask for build execution') {
            agent none
            when {
                not {
                    anyOf {
                        branch "develop*"
                        branch "master_*"
                        tag pattern: "^[1-9]+(\\.rc)?(\\.[0-9]+)?\\.[0-9]+(-.*)?", comparator: "REGEXP"
                    }
                }
            }
            steps {
                script {
                    INPUT_PARAMS = input message: 'Check boxes to select what you want to execute ?',
                    parameters: [
                        booleanParam(name: 'DO_MAJ_CONTEXT', defaultValue: true, description: 'Run Stage Upgrade build context.'),
                        booleanParam(name: 'DO_TEST', defaultValue: false, description: 'Run Stage Check vulnerabilities and tests.'),
                        booleanParam(name: 'DO_BUILD', defaultValue: true, description: 'Run Stage Build sources & COTS.'),
                        booleanParam(name: 'DO_PUBLISH', defaultValue: true, description: 'Run Stage Publish to repository.'),
                        booleanParam(name: 'DO_CHECKMARX', defaultValue: false, description: 'Run Stage Checkmarx analysis.'),
                        booleanParam(name: 'DO_CHECKMARX_SCA', defaultValue: false, description: 'Run Stage Checkmarx SCA.')
                    ]
                    env.DO_MAJ_CONTEXT = INPUT_PARAMS.DO_MAJ_CONTEXT
                    env.DO_TEST = INPUT_PARAMS.DO_TEST
                    env.DO_BUILD = INPUT_PARAMS.DO_BUILD
                    env.DO_PUBLISH = INPUT_PARAMS.DO_PUBLISH
                    env.DO_CHECKMARX = INPUT_PARAMS.DO_CHECKMARX
                    env.DO_CHECKMARX_SCA = INPUT_PARAMS.DO_CHECKMARX_SCA
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
                sh 'sudo yum remove -y nodejs'
                sh "curl -fsSL ${env.NODE_JS_DOWNLOAD_URL} | sudo -E bash -"
                sh 'sudo yum --enablerepo=nodesource clean all'
                sh 'sudo yum install -y nodejs gcc-c++ make'
                sh 'sudo rm -f /usr/local/bin/node /usr/local/bin/npm'
                sh 'node -v;npm -v'
            }
        }

        stage('Check vulnerabilities and tests') {
            when {
                environment(name: 'DO_TEST', value: 'true')
            }
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
                NODE_OPTIONS="--max_old_space_size=12288"
            }
            steps {
                sh 'node -v'
                sh 'npmrc default'

                sh '''
                    $MVN_COMMAND clean verify -U -Pvitam -pl  '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express' $JAVA_TOOL_OPTIONS
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build sources') {
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
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
                http_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
                https_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
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

        stage("Get publishing scripts") {
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

        stage("Publish rpm and deb") {
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

        stage("Update symlink") {
            when {
                anyOf {
                    branch "develop"
                    branch "master_*"
                    tag pattern: "^[1-9]+(\\.rc)?(\\.[0-9]+)?\\.[0-9]+(-.*)?", comparator: "REGEXP"
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

        stage("Checkmarx analysis") {
            when {
                environment(name: 'DO_CHECKMARX', value: 'true')
            }
            environment {
                SERVICE_CHECKMARX_URL = credentials("service-checkmarx-url")
            }
            steps {
                dir('vitam-build.git') {
                    deleteDir()
                }
                sh 'mkdir -p target logs'
                // KWA : Visibly, backslash escape hell. \\ => \ in groovy string.
                sh '/opt/CxConsole/runCxConsole.sh scan --verbose -Log "${PWD}/logs/cxconsole.log" -CxServer "$SERVICE_CHECKMARX_URL" -CxUser "VITAM openLDAP\\\\$CI_USR" -CxPassword \\"$CI_PSW\\" -ProjectName "CxServer\\SP\\Vitam\\Users\\vitam-ui $GIT_BRANCH" -LocationType folder -locationPath "${PWD}/" -Preset "Default 2014" -LocationPathExclude "cots,deployment,deploymentByVitam,docs,integration-tests,tools,node,node_modules,dist,target" -LocationFilesExclude "*.rpm,*.pdf" -ForceScan -ReportPDF "${PWD}/target/checkmarx-report.pdf"'
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
        stage('Checkmarx SCA') {
           when {
                environment(name: 'DO_CHECKMARX_SCA', value: 'true')
           }
           environment {
                SERVICE_CX_SCA_USER = credentials("service-cx-sca-user")
                SERVICE_CX_SCA_PASSWORD = credentials("service-cx-sca-password")
                SERVICE_CX_SCA_ACCOUNT = credentials("service-cx-sca-account")
                SERVICE_CX_SCA_SERVER = credentials("service-cx-sca-server")
                SERVICE_CX_SCA_AUTH_SERVER = credentials("service-cx-sca-auth-server")

                http_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
                https_proxy="http://${env.SERVICE_PROXY_HOST}:${env.SERVICE_PROXY_PORT}"
                CX_NAME="vitam-ui.${env.GIT_BRANCH}"
           }
           steps {
                sh 'curl -O https://sca-downloads.s3.amazonaws.com/cli/latest/ScaResolver-linux64.tar.gz'
                sh 'tar -xzvf ScaResolver-linux64.tar.gz'
                sh 'sudo ln -sf /usr/local/maven/bin/mvn /usr/local/bin/mvn'
                sh './ScaResolver -n $CX_NAME -u $SERVICE_CX_SCA_USER -a $SERVICE_CX_SCA_ACCOUNT --server-url $SERVICE_CX_SCA_SERVER --authentication-server-url $SERVICE_CX_SCA_AUTH_SERVER -s ui -p "$SERVICE_CX_SCA_PASSWORD" --report-type Risk --report-extension Pdf'
           }
           post {
                success {
                    archiveArtifacts (
                        artifacts: "reports/$CX_NAME/*.pdf",
                        fingerprint: true
                    )
                }
                failure {
                    archiveArtifacts (
                        artifacts: "logs/$CX_NAME/*.log",
                        fingerprint: true
                    )
                }
            }
        }
    }

    post {
        // Clean after build
        always {

            // Cleanup any remaining docker volumes
            sh 'docker volume prune -f'

            // Cleanup M2 repo
            sh 'rm -fr ${M2_REPO}/repository/fr/gouv/vitamui/'

            // Cleanup workspace
            cleanWs()
        }
    }
}
