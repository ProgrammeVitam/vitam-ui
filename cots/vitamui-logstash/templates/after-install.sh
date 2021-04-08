# Ensure rights for vitamui service file system
chown __USER__:__GROUP__  -R   /vitamui/*/__NAME__/
chown vitamui:vitamui /vitamui/ /vitamui/*
chown -R vitamuidb:vitamui /vitamui/ /vitamui/*/*
chmod 0555 /vitamui/ /vitamui/*
chmod 0750 /vitamui/*/*

# Initial installation
systemctl daemon-reload