def IMPORTANT_BRANCH_OR_TAG = (env.BRANCH_NAME =~ /(develop|master_.*)/).matches() || env.TAG_NAME != null

pipeline {
    agent {
        label 'java21'
    }

    environment {
        M2_REPO = "${HOME}/.m2"
        CI = credentials("app-jenkins")

        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_REPOSITORY_URL = credentials("service-repository-url")
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

    parameters {
        choice(name: 'GOAL', choices: ['publish', 'deploy', 'build'], description: '- "build" only builds the artifacts\n- "deploy" builds and pushes the artifacts in Nexus\n- "publish" deploys and pushes the .deb/.rpm to the repository')
        booleanParam(name: 'DO_CHECKS_AND_TESTS', defaultValue: IMPORTANT_BRANCH_OR_TAG, description: 'Tick the box to run checks and tests')
    }

    stages {
        stage('Ask for build execution (when parameters are not defined)') {
            agent none
            when { expression { env.DO_CHECKS_AND_TESTS == null } }
            steps {
                script {
                    INPUT_PARAMS = input message: 'Configure your build',
                        parameters: [
                            choice(name: 'GOAL', choices: 'publish\ndeploy\nbuild', defaultValue: 'publish', description: '- "build" only builds the artifacts\n- "deploy" builds and pushes the artifacts in Nexus\n- "publish" deploys and pushes the .deb/.rpm to the repository'),
                            booleanParam(name: 'DO_CHECKS_AND_TESTS', defaultValue: IMPORTANT_BRANCH_OR_TAG, description: 'Tick the box to run checks and tests'),
                        ]

                    env.GOAL = INPUT_PARAMS.GOAL
                    env.DO_CHECKS_AND_TESTS = INPUT_PARAMS.DO_CHECKS_AND_TESTS
                }
            }
        }

        stage('Show Configuration') {
            steps {
                script {
                    if (env.GOAL == 'build') {
                        // If the goal is only to build, we only run "mvn verify" (includes running tests, except if explicitely skipped)
                        env.MVN_GOAL = 'verify'
                    } else {
                        // Otherwise, we run "mvn deploy" to generate the artifact and upload it to Nexus
                        env.MVN_GOAL = 'deploy'
                    }

                    env.MVN_COMMAND = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml --show-version --batch-mode --errors -DdeployAtEnd=true"
                    if (env.DO_CHECKS_AND_TESTS == 'false') {
                        // If checks and tests are disabled:
                        // - "-T1C" builds modules in parallel
                        // - "-Dspotless.check.skip=true" skips executing spotless
                        // - "-Dmaven.test.skip" skips executing tests
                        // - "-Dlicense.skip" skips checking license headers
                        env.MVN_COMMAND = "${env.MVN_COMMAND} -T1C -Dspotless.check.skip=true -Dmaven.test.skip -Dlicense.skip=true"
                    }

                    def pom = readMavenPom file: 'pom.xml'
                    env.POM_VERSION = pom.version
                }
                echo "IMPORTANT_BRANCH_OR_TAG = ${env.IMPORTANT_BRANCH_OR_TAG}"
                echo "GOAL = ${env.GOAL}"
                echo "MVN_GOAL = ${env.MVN_GOAL}"
                echo "DO_CHECKS_AND_TESTS = ${env.DO_CHECKS_AND_TESTS}"
                echo "MVN_COMMAND = ${env.MVN_COMMAND}"
                echo "POM_VERSION = ${env.POM_VERSION}"
            }
        }

        stage('Upgrade build context') {
            steps {
                sh 'sudo apt install -y build-essential make ruby ruby-dev rubygems jq'
                sh 'sudo timedatectl set-timezone Europe/Paris'
                sh 'sudo gem install fpm'
            }
        }

        stage('Parallel') {
            parallel {
                stage('Check icomoon') {
                    when {
                        environment(name: 'DO_CHECKS_AND_TESTS', value: 'true')
                    }
                    steps {
                        sh './tools/check_icomoon.sh'
                    }
                }
                stage('Frontend') {
                    steps {
                        dir('ui/ui-frontend') {
                            script {
                                nvm('v18.20.3') {
                                    sh 'npm ci'
                                    if (env.DO_CHECKS_AND_TESTS == 'true') {
                                        sh 'npm run lint'
                                    }
                                    sh 'npm run build:vitamui-library'
                                    sh 'npm run build:allModules'
                                    if (env.DO_CHECKS_AND_TESTS == 'true') {
                                        sh 'npm run ci:test'
                                    }
                                }
                                if (env.GOAL == 'publish') {
                                    // If the goal is to publish, we also generate .deb/.rpm
                                    sh '../../tools/packaging/package-fronts.sh ui-identity,ui-archive-search,ui-portal,ui-pastis,ui-collect,ui-referential,ui-ingest,ui-design-system ${POM_VERSION}'
                                }
                            }
                        }
                    }
                }
                stage('Backend') {
                    tools {
                        jdk 'java21' // java11 || java17 || java21
                        maven 'maven-3.9' // maven-3.8 || maven-3.9
                    }
                    steps {
                        // TODO: generate .deb/.rpm by running Makefile directly in the Jenkinsfile instead of being run by a maven plugin
                        nvm('v18.20.3') { // We need node for spotless (when env.DO_CHECKS_AND_TESTS == 'true')
                            sh '${MVN_COMMAND} clean ${MVN_GOAL} -U -Pvitam,deb,rpm'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        if (env.DO_CHECKS_AND_TESTS == 'true') {
                            junit '**/target/surefire-reports/*.xml'
                            junit '**/target/junit/*.xml'
                        }
                    }
                }
            }
        }

        // If in "deploy" or "publish" mode, build pastis front in "standalone" mode & run maven goal to package the .exe/.zip
        stage("Pastis standalone") {
            when {
                anyOf {
                    environment(name: 'GOAL', value: 'deploy')
                    environment(name: 'GOAL', value: 'publish')
                }
            }
            steps {
                dir('ui/ui-frontend') {
                    nvm('v18.20.3') {
                        sh 'npm run build:pastis-standalone'
                    }
                }
                sh '${MVN_COMMAND} deploy -Pstandalone --projects "api/api-pastis/pastis-standalone" -Dspotless.check.skip=true -Dmaven.test.skip -Dlicense.skip=true'
            }
        }

        stage("Publish") {
            when {
                environment(name: 'GOAL', value: 'publish')
            }
            steps {
                script {
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: 'scaleway_j11']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'vitam-build.git']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'app-jenkins', url: "$SERVICE_GIT_URL"]]
                    ])
                    sshagent(credentials: ['jenkins_sftp_to_repository']) {
                        sh 'vitam-build.git/push_vitamui_repo.sh contrib ${SERVICE_REPO_SSHURL} rpm'
                        sh 'vitam-build.git/push_vitamui_repo.sh contrib ${SERVICE_REPO_SSHURL} deb'
                    }
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
                environment(name: 'GOAL', value: 'publish')
            }
            steps {
                sshagent(credentials: ['jenkins_sftp_to_repository']) {
                    sh 'vitam-build.git/push_symlink_repo.sh contrib ${SERVICE_REPO_SSHURL}'
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
