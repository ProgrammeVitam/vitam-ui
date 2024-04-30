pipeline {
    agent {
        label 'contrib'
    }

    environment {
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        M2_REPO = "${HOME}/.m2"
        CI = credentials("app-jenkins")

        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_REPOSITORY_URL=credentials("service-repository-url")

        PUPPETEER_DOWNLOAD_HOST="${SERVICE_NEXUS_URL}repository/puppeteer-chrome"
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

    stages {
        stage('Set variables for automatic run') {
            agent none
            steps {
                script {
                    env.DO_BUILD_AND_TEST = 'true'
                    env.DO_DEPLOY = 'true'
                    env.DO_DEPLOY_PASTIS_STANDALONE = 'true'
                    env.DO_PUBLISH = 'true'
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
                        booleanParam(name: 'DO_BUILD_AND_TEST', defaultValue: true, description: 'Run Stage Build and test'),
                        booleanParam(name: 'DO_DEPLOY', defaultValue: false, description: 'Run Stage Deploy to Nexus'),
                        booleanParam(name: 'DO_DEPLOY_PASTIS_STANDALONE', defaultValue: false, description: 'Run build stage Deploy PASTIS standalone'),
                        booleanParam(name: 'DO_PUBLISH', defaultValue: false, description: 'Run Stage Publish to repository.'),
                    ]
                    env.DO_BUILD_AND_TEST = INPUT_PARAMS.DO_BUILD_AND_TEST
                    env.DO_DEPLOY = INPUT_PARAMS.DO_DEPLOY
                    env.DO_DEPLOY_PASTIS_STANDALONE = INPUT_PARAMS.DO_DEPLOY_PASTIS_STANDALONE
                    env.DO_PUBLISH = INPUT_PARAMS.DO_PUBLISH
                }
            }
        }

        stage('Upgrade build context') {
            steps {
                sh 'sudo apt remove -y nodejs npm node-npmrc'
                sh 'sudo apt install -y build-essential make ruby ruby-dev rubygems jq'
                sh 'sudo rm -f /usr/local/bin/node /usr/local/bin/npm'
                sh 'sudo timedatectl set-timezone Europe/Paris'
                sh 'sudo gem install fpm'
            }
        }

        stage('Build and test') {
            when {
                environment(name: 'DO_BUILD_AND_TEST', value: 'true')
            }
            steps {
                sh './tools/check_icomoon.sh'

                sh '''
                    $MVN_COMMAND clean verify -U -Pvitam \
                        --projects '!cots/vitamui-mongo-express' \
                        --projects '!ui/ui-archive-search' \
                        --projects '!ui/ui-collect' \
                        --projects '!ui/ui-commons' \
                        --projects '!ui/ui-identity' \
                        --projects '!ui/ui-ingest' \
                        --projects '!ui/ui-pastis' \
                        --projects '!ui/ui-portal' \
                        --projects '!ui/ui-referential'
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    junit '**/target/junit/*.xml'
                }
            }
        }

        stage('Deploy to Nexus') {
            when {
                environment(name: 'DO_DEPLOY', value: 'true')
            }
            steps {
                sh '''
                    $MVN_COMMAND deploy -Pvitam,deb,rpm -DskipTests -DskipAllFrontend=true -DskipAllFrontendTests=true -Dlicense.skip=true --projects '!cots/vitamui-mongo-express'
                '''
            }
        }

        stage('Deploy PASTIS standalone') {
            when {
                environment(name: 'DO_DEPLOY_PASTIS_STANDALONE', value: 'true')
            }
            steps {
                sh '''
                    $MVN_COMMAND install \
                        -D skipTests \
                        -P vitam \
                        --projects '!ui/ui-archive-search' \
                        --projects '!ui/ui-collect' \
                        --projects '!ui/ui-commons' \
                        --projects '!ui/ui-identity' \
                        --projects '!ui/ui-ingest' \
                        --projects '!ui/ui-pastis' \
                        --projects '!ui/ui-portal' \
                        --projects '!ui/ui-referential'
                '''
                sh '''
                    $MVN_COMMAND deploy \
                        -D skipTests \
                        -P standalone \
                        --projects 'api/api-pastis/pastis-standalone'
                '''
            }
        }

        stage('Build COTS') {
            when {
                environment(name: 'DO_DEPLOY', value: 'true')
            }
            steps {
                dir('cots/') {
                    sh '''
                        $MVN_COMMAND deploy -Pvitam,deb,rpm -DskipTests -Dlicense.skip=true
                    '''
                }
            }
        }

        stage("Get publishing scripts") {
            when {
                environment(name: 'DO_PUBLISH', value: 'true')
                environment(name: 'DO_DEPLOY', value: 'true')
            }
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: 'scaleway_j11']],
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
                environment(name: 'DO_DEPLOY', value: 'true')
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
                environment(name: 'DO_DEPLOY', value: 'true')
            }
            steps {
                sshagent (credentials: ['jenkins_sftp_to_repository']) {
                    sh 'vitam-build.git/push_symlink_repo.sh contrib $SERVICE_REPO_SSHURL'
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
