# After first install
if [ $1 -eq 1 ] ; then
        # Ensure rights for vitamui service file system
        chown __USER__:__GROUP__  -R   /vitamui/*/__NAME__/
        chown vitamui:vitamui /vitamui/ /vitamui/*
        chown -R vitamuidb:vitamui /vitamui/ /vitamui/*/*
        chmod 0555 /vitamui/ /vitamui/*
        chmod 0750 /vitamui/*/*

        # Initial installation
        systemctl preset  vitamui-logstash.service >/dev/null 2>&1 || :
fi
