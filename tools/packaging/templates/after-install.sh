# After first install
#if [ $1 -eq 1 ] ; then
        # Ensure rights for vitamui service file system (symbolic links are not followed)
        for dir in /vitamui/*/__NAME__; do
                if [ -d "$dir" ] && [ ! -L "$dir" ]; then
                        chown -R __USER__:__GROUP__ "$dir"
                fi
        done
        find /vitamui/ -mindepth 1 -maxdepth 1 ! -type l -exec chown vitamui:vitamui {} + -exec chmod 0555 {} +
        chown vitamui:vitamui /vitamui/
        chmod 0555 /vitamui/
        find /vitamui/ -mindepth 2 -maxdepth 2 ! -type l -exec chmod 0750 {} +

        # Initial installation
        systemctl preset __USER__-__NAME__.service #>/dev/null 2>&1 || :
#fi
