#if [ $1 -eq 0 ] ; then
        # Package removal, not upgrade
        systemctl --no-reload disable vitamui-collect.service #> /dev/null 2>&1 || :
        systemctl stop vitamui-collect.service #> /dev/null 2>&1 || :
#fi
