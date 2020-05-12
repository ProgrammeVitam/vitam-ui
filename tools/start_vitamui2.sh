#!/usr/bin/env bash
#set -x

# This script starts vitamui modules

# The dev environnement use node.js as front
# Http  ports are 4200, 4201, etc.
# Modules needs to be compiled with the default profile :
# mvn clean install -DskipTests

# The prod environnement put the angular webapp in a jar with tomcat embedded
# Http ports are 8200, 8201, etc.
# Modules needs to be compiled with the webpack profile :
# mvn clean install -Pwebpack -DskipTests

# To use debug mode :
# mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

export MAVEN_OPTS="-Xmx50M -XX:+UseG1GC"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
# SPRINGBOOT="mvn spring-boot:run -Dspring-boot.run.noverify"
SPRINGBOOT='mvn -Pvitam --batch-mode spring-boot:run -Dspring-boot.run.noverify -Dspring-boot.run.jvmArguments='\''-XX:+UseG1GC'\'
NPMSTART="sh -c 'npm install; npm run start'"

if [[ "$*" =~ --erase ]]
then
     rm "${DIR}"/logs/*
fi    

log_count=0

function launch() {
     if [[ ! -e "${DIR}/logs" ]] ; then mkdir "${DIR}/logs" ; fi
     echo ;    echo =========== Starting $1 ========== ;     echo
     #gnome-terminal --disable-factory --title="$(basename $1)" --working-directory="${DIR}/$1" -e "$2" &>/dev/null &

     ((log_count += 1))
     pushd "${DIR}/$1" > /dev/null || exitm
     eval $2 > "${DIR}/logs/$(basename "$1")-$log_count.log" 2> "${DIR}/logs/$(basename "$1")-error.$log_count.log" &
     echo "$2 &"
     popd > /dev/null  || exit

     #echo "$! " >> /tmp/external.pids
}

function clean() {
     mkdir -p $1/target/src/main
     rm -rf $1/target/src/main/config
     cp -r $1/src/main/config $1/target/src/main/config
}

function cmd() {
     echo $1 $2
     pushd $1 ; $2 ; popd
}

function start_api() {

     # Start Iam Security
     launch "../api/api-security/security-internal" "$SPRINGBOOT"

     sleep 15

     # Start Iam Server Internal
     launch "../api/api-iam/iam-internal" "$SPRINGBOOT"

     sleep 15

     # Start Iam Server External
     launch "../api/api-iam/iam-external" "$SPRINGBOOT"

     sleep 15

     # Start Cas Server
     launch "../cas/cas-server" "java -Xmx200M -XX:+UseG1GC -Dspring.config.additional-location=src/main/config/cas-server-application-dev.yml -jar target/cas-server.war"

     sleep 15

}

function start_ui_prod() {

     # Start UI Identity
     clean "../ui/ui-identity"
     launch "../ui/ui-identity" "./target/ui-identity-${BRANCH}.jar --spring.config.additional-location=file:src/main/config/ui-identity-application-recette.yml"

     # Start UI Portal
     clean "../ui/ui-portal"
     launch "../ui/ui-portal" "./target/ui-portal-${BRANCH}.jar --spring.config.additional-location=file:src/main/config/ui-portal-application-recette.yml"
}

function start_ui_back_dev() {

     # Start UI Identity back
     clean "../ui/ui-identity"
     launch "../ui/ui-identity" "$SPRINGBOOT"

     sleep 15

     # Start UI Portal back
     clean "../ui/ui-portal"
     launch "../ui/ui-portal" "$SPRINGBOOT"
     
     sleep 15
}

function start_ui_front_dev {

     # Start UI Identity front
#     cmd "ui/ui-frontend" "$NPMINSTALL"
     launch "../ui/ui-frontend" "$NPMSTART:identity"

     # Start UI Portal front
#     cmd "ui/ui-frontend" "$NPMINSTALL"
     launch "../ui/ui-frontend" "$NPMSTART:portal"
}

./stop_external.sh &> /dev/null

echo
echo =================================
echo STARTING INTEGRATION ENVIRONNEMENT
echo =================================
echo
(
     flock -e -n 200

     echo =========== Starting MONGO ==========
     case $(docker inspect -f \{\{.State.Running\}\} vitamui-mongo) in
          "false")
               docker start vitamui-mongo
               ;;
          "true")
               # do nothing
               ;;
          *)
               pushd docker/mongo ; ./start_dev.sh ; popd
          ;;
     esac

     BRANCH=$(grep -oP '(?<=>).*?(?=</version>)' ../api/pom.xml | grep -v 'version')

     case $1 in
         api-only)
              start_api
              ;;
         back)
              start_api
              start_ui_back_dev
             ;;
	    front)
             start_ui_front_dev
             ;;
         all-ui-prod)
             start_api
             start_ui_prod
             ;;
          *)
             start_api
             start_ui_back_dev
             start_ui_front_dev
             ;;
	 esac

) 200>/tmp/external.lockfile

echo
echo =================================
echo INTEGRATION ENVIRONNEMENT STARTED
echo =================================
echo
