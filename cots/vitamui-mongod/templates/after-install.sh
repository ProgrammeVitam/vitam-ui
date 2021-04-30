# Ensure rights for vitamui service file system
chown __USER__:__GROUP__  -R   /vitamui/*/__NAME__/
chown vitamui:vitamui /vitamui/ /vitamui/*
chmod 0555 /vitamui/ /vitamui/*
chmod 0750 /vitamui/*/*

# Initial installation
systemctl preset vitamui-mongod.service 
        # Disable os package installed service to not run it for nothing
systemctl disable mongod.service 
