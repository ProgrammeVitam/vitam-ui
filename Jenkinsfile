pipeline {
    agent {
        label 'contrib'
    }

    environment {
        SLACK_MESSAGE = "${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}|Open>)"
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        M2_REPO = "${HOME}/.m2"
        CI = credentials("app-jenkins")

        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_REPOSITORY_URL=credentials("service-repository-url")
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
                    env.DO_MAJ_CONTEXT = 'true'
                    env.DO_TEST = 'true'
                    env.DO_BUILD = 'true'
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
                        booleanParam(name: 'DO_MAJ_CONTEXT', defaultValue: true, description: 'Run Stage Upgrade build context.'),
                        booleanParam(name: 'DO_TEST', defaultValue: false, description: 'Run Stage Check vulnerabilities and tests.'),
                        booleanParam(name: 'DO_BUILD', defaultValue: true, description: 'Run Stage Build sources & COTS.'),
                        booleanParam(name: 'DO_PUBLISH', defaultValue: true, description: 'Run Stage Publish to repository.'),
                    ]
                    env.DO_MAJ_CONTEXT = INPUT_PARAMS.DO_MAJ_CONTEXT
                    env.DO_TEST = INPUT_PARAMS.DO_TEST
                    env.DO_BUILD = INPUT_PARAMS.DO_BUILD
                    env.DO_PUBLISH = INPUT_PARAMS.DO_PUBLISH
                }
            }
        }

        stage('Upgrade build context') {
            when {
                environment(name: 'DO_MAJ_CONTEXT', value: 'true')
            }
            steps {
                sh 'sudo apt remove -y nodejs'
                sh 'sudo apt install -y nodejs npm node-npmrc build-essential make ruby ruby-dev rubygems'
                sh 'sudo rm -f /usr/local/bin/node /usr/local/bin/npm'
                sh 'node -v;npm -v'
                sh 'sudo timedatectl set-timezone Europe/Paris'
                sh 'sudo gem install fpm  '
            }
        }

        stage('Check vulnerabilities and tests') {
            when {
                environment(name: 'DO_TEST', value: 'true')
            }
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}repository/puppeteer-chrome"
                NODE_OPTIONS="--max_old_space_size=12288"
            }
            steps {
                sh 'node -v'
                sh 'npmrc default'

                sh '''
                    $MVN_COMMAND clean verify -e -U -Pvitam -pl '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express'
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
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}repository/puppeteer-chrome"
            }
            when {
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sh 'npmrc default'
                sh '''
                    $MVN_COMMAND deploy -Pvitam,deb,rpm -DskipTests -DskipAllFrontend=true -DskipAllFrontendTests=true -Dlicense.skip=true -pl '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express'
                '''
            }
        }

        stage('Build PASTIS standalone') {
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}repository/puppeteer-chrome"
            }
            when {
                environment(name: 'DO_BUILD', value: 'true')
            }
            steps {
                sh 'npmrc default'
                sh '''
                    $MVN_COMMAND deploy -Pstandalone -DskipTests -DskipAllFrontend=true -DskipAllFrontendTests=true -Dlicense.skip=true -pl api/api-pastis/pastis-standalone
                '''
            }
        }

        stage('Build COTS') {
            when {
                environment(name: 'DO_BUILD', value: 'true')
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
                environment(name: 'DO_BUILD', value: 'true')
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
