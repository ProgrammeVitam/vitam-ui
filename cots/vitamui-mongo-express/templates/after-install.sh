# After first install
if [ $1 -eq 1 ] ; then
        # Ensure rights for vitamui service file system
        chown vitamuidb:vitamui  -R   /vitamui/*/__NAME__/
        chown vitamui:vitamui /vitamui/ /vitamui/*
        chmod 0555 /vitamui/ /vitamui/*
        chmod 0750 /vitamui/*/*

        # Initial installation
        systemctl preset vitamui-mongo-express.service >/dev/null 2>&1 || :
fi
