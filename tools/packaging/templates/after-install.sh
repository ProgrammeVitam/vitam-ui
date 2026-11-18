# After first install
#if [ $1 -eq 1 ] ; then
        # Ensure rights for vitamui service file system
        # Symbolic links are not followed, except a service directory linked by root to another volume
        for dir in /vitamui/*/__NAME__; do
                if [ -d "$dir" ] && { [ ! -L "$dir" ] || [ "$(stat -c %u "$dir")" -eq 0 ]; }; then
                        chown -RH __USER__:__GROUP__ "$dir"
                        # The find commands below skip symbolic links: set the mode of the link target
                        if [ -L "$dir" ]; then
                                chmod 0750 "$dir"
                        fi
                fi
        done
        find /vitamui/ -mindepth 1 -maxdepth 1 ! -type l ! -name '.*' -exec chown vitamui:vitamui {} + -exec chmod 0555 {} +
        chown vitamui:vitamui /vitamui/
        chmod 0555 /vitamui/
        find /vitamui/ -mindepth 2 -maxdepth 2 ! -type l ! -name '.*' ! -path '/vitamui/.*' -exec chmod 0750 {} +

        # Initial installation
        systemctl preset __USER__-__NAME__.service #>/dev/null 2>&1 || :
#fi
