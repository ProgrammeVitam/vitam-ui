# Ensure rights for vitamui service file system
chown vitamuidb:vitamui  -R   /vitamui/*/__NAME__/
chown vitamui:vitamui /vitamui/ /vitamui/*
chmod 0555 /vitamui/ /vitamui/*
chmod 0750 /vitamui/*/*

systemctl daemon-reload

