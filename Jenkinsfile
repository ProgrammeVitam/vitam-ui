pipeline {
    agent {
        label 'contrib'
    }

    environment {
        SLACK_MESSAGE = "${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}|Open>)"
        MVN_BASE = "/usr/local/maven/bin/mvn --settings ${pwd()}/.ci/settings.xml"
        MVN_COMMAND = "${MVN_BASE} --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        CI = credentials("app-jenkins")
        SERVICE_SONAR_URL = credentials("service-sonar-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_PROXY_HOST = credentials("http-proxy-host")
        SERVICE_PROXY_PORT = credentials("http-proxy-port")
        NOPROXY_HOST = credentials("http_nonProxyHosts")
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

    // triggers {
    //     cron('45 2 * * *')
    // }

    stages {

        stage('Build sources') {
            environment {
                PUPPETEER_DOWNLOAD_HOST="${env.SERVICE_NEXUS_URL}/repository/puppeteer-chrome/"
            }
            steps {
                sh 'npmrc default'
                sh '''
                    $MVN_COMMAND deploy -Pvitam,rpm,webpack -Dlicense.skip=true -pl '!cots/vitamui-nginx,!cots/vitamui-mongod,!cots/vitamui-logstash,!cots/vitamui-mongo-express' $JAVA_TOOL_OPTIONS
                '''
            }
        }
    }
}
