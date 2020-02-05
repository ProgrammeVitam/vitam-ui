#!/usr/bin/env bash
# Emmanuel Deviller

echo
echo =================================
echo STOPPING ENVIRONNEMENT
echo =================================
echo

(
    flock -e -n 200

#    if [ -s /tmp/external.pids ]
#    then
#        echo Stopping SpringBoot Services
#        kill $(cat /tmp/external.pids)
#        rm -f /tmp/external.pids
#    fi

    # Arghhhh!
    # kill $(netstat -plantu 2> /dev/null | grep '8081\|8082\|4200\|4201' | awk '{print $7}' | awk -F'/' '{print $1}')
    kill -9 $(ps aux | grep java | grep -v "idea" | grep -v "eclipse"  | awk '{print $2}')

    # Stop Mongo
    echo Stopping Mongo
    pushd docker/mongo
    ./stop_dev.sh
    popd

) 200>/tmp/external.lockfile

echo
echo =================================
echo INTEGRATION ENVIRONNEMENT STOPPED
echo =================================
echo
