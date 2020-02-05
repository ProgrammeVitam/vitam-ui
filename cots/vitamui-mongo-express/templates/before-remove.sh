if [ $1 -eq 0 ] ; then
        # Package removal, not upgrade
        systemctl --no-reload disable vitamui-mongo-express.service >/dev/null 2>&1 || :
        systemctl stop vitamui-mongo-express.service >/dev/null 2>&1 || :
fi
