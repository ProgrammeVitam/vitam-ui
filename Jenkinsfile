def IMPORTANT_BRANCH_OR_TAG = (env.BRANCH_NAME =~ /(develop|master_.*)/).matches() || env.TAG_NAME != null

pipeline {
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        metadata:
          labels:
            some-label: buildpod
        spec:
          affinity:
            nodeAffinity:
              requiredDuringSchedulingIgnoredDuringExecution:
                nodeSelectorTerms:
                - matchExpressions:
                  - key: k8s.scaleway.com/pool-name
                    operator: In
                    values:
                    - pool-par-2-practical-noyce
          containers:
          - name: maven
            image: harbor-k8s.programmevitam.fr/library/vitam-buildkit:1.0
            env:
             - name: TZ
               value: Europe/Paris
             - name: JDK_JAVA_OPTIONS
               value: "-XX:+HeapDumpOnOutOfMemoryError -Xms4g -Xmx8g"
             - name: MAVEN_OPTS
               value: "-Xmx8000m"
             - name: NODE_OPTIONS
               value: "--max-old-space-size=2048"
             - name: LANG
               value: "C.UTF-8"
             - name: LC_ALL
               value: "C.UTF-8"
            resources:
              requests:
                memory: "16Gi"
                cpu: "2"
            limits:
                memory: "32Gi"
                cpu: "3"
            command:
            - cat
            tty: true
          imagePullSecrets:
          - name: harbork8scred
'''
    }
  }

    environment {
        M2_REPO = "${HOME}/.m2"
        CI = credentials("app-jenkins")

        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_NEXUS_URL = "http://172.16.0.43"
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_REPOSITORY_URL = "https://repository.dev.programmevitam.fr"
        DOCKER_HOST = "tcp://kubedock-service.tools:2475"
        TESTCONTAINERS_RYUK_DISABLED = true
        TESTCONTAINERS_CHECKS_DISABLE = true
        CHROME_BIN = "/usr/bin/google-chrome"
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
                container('maven') {
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

        }

        stage('Show Configuration') {
            steps {
                container('maven') {
                script {
                    if (env.GOAL == 'build') {
                        // If the goal is only to build, we only run "mvn verify" (includes running tests, except if explicitely skipped)
                        env.MVN_GOAL = 'verify'
                    } else {
                        // Otherwise, we run "mvn deploy" to generate the artifact and upload it to Nexus
                        env.MVN_GOAL = 'deploy'
                    }

                    env.MVN_COMMAND = "mvn --settings ${pwd()}/.ci/settings.xml --show-version --batch-mode --errors -DdeployAtEnd=true"
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
        }

        stage('Upgrade build context') {
            steps {
                container('maven') {
                sh 'date'
      /*          sh 'apt  update  || true'
                sh 'echo "deb http://deb.debian.org/debian bookworm main contrib non-free" >> /etc/apt/sources.list'
                sh 'echo "deb http://deb.debian.org/debian bookworm-updates main contrib non-free" >> /etc/apt/sources.list'
                sh 'apt install -y rpm'
                sh 'apt install -y build-essential make ruby ruby-dev rubygems jq nodejs wget'
                sh 'apt install -y npm'
                sh 'gem install fpm'
                sh 'npm -v'
                sh 'wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb'
                sh 'apt-get install -y fonts-liberation libasound2 libatk-bridge2.0-0 libatk1.0-0 libatspi2.0-0 libcairo2 libcups2 libgtk-3-0 libnspr4 libnss3 libpango-1.0-0 libvulkan1 libxdamage1 libxkbcommon0'
                sh 'dpkg -i google-chrome-stable_current_amd64.deb'
                sh 'id' */
                sh 'env'
                //sh 'sleep 600'

                }
            }
        }

        stage('Parallel') {
            parallel {
                stage('Check icomoon') {
                    when {
                        environment(name: 'DO_CHECKS_AND_TESTS', value: 'true')
                    }
                    steps {
                        container('maven') {
                        sh './tools/check_icomoon.sh'
                    }
                    }

                }
                stage('Frontend') {
                    steps {
                        container('maven') {
                        dir('ui/ui-frontend') {
                            script {
                                    sh 'npm ci'
                                    if (env.DO_CHECKS_AND_TESTS == 'true') {
                                        sh 'npm run lint'
                                    }
                                    sh 'npm run build:vitamui-library'
                                    sh 'npm run build:allModules'
                                    if (env.DO_CHECKS_AND_TESTS == 'true') {
                                        sh 'npm run ci:test'
                                    }

                                if (env.GOAL == 'publish') {
                                    // If the goal is to publish, we also generate .deb/.rpm
                                    sh '../../tools/packaging/package-fronts.sh ui-identity,ui-archive-search,ui-portal,ui-pastis,ui-collect,ui-referential,ui-ingest,ui-design-system ${POM_VERSION}'
                                }
                            }
                        }
                        }
                    }
                }
                stage('Backend') {
                    steps {
                        container('maven') {
                        // TODO: generate .deb/.rpm by running Makefile directly in the Jenkinsfile instead of being run by a maven plugin
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
                container('maven') {
                dir('ui/ui-frontend') {
                    // nvm('v18.20.3') {
                        sh 'npm run build:pastis-standalone'
                    // }
                }
                sh '${MVN_COMMAND} deploy -Pstandalone --projects "api/api-pastis/pastis-standalone" -Dspotless.check.skip=true -Dmaven.test.skip -Dlicense.skip=true'
            }
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
}
