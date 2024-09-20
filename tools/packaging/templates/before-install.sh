echo "__NAME__ : $0" >> /root/package-history.log
# Init vitamui system group
getent group vitamui >/dev/null || groupadd -g 4000 vitamui

# Init vitamui system user
getent passwd vitamui >/dev/null || useradd -u 4000 -g 4000 -s /bin/bash -c "vitamui service user" vitamui
