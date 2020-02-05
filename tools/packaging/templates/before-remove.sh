if [ $1 -eq 0 ] ; then
        # Package removal, not upgrade
        systemctl --no-reload disable __USER__-__NAME__.service > /dev/null 2>&1 || :
        systemctl stop __USER__-__NAME__.service > /dev/null 2>&1 || :
fi
