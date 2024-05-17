# After first install
#if [ $1 -eq 1 ] ; then
        # Ensure rights for vitamui service file system
        chown vitamui:vitamui  -R   /vitamui/*/identity/
        chown vitamui:vitamui /vitamui/ /vitamui/*
        chmod 0555 /vitamui/ /vitamui/*
        chmod 0750 /vitamui/*/*

        # Initial installation
        systemctl preset vitamui-identity.service #>/dev/null 2>&1 || :
#fi
