echo "__NAME__-__VERSION__ : (before-install-rsc.sh)" >> /root/package-history.log

getent group  vitamui >/dev/null || groupadd -g 4000 vitamui
getent passwd vitamui >/dev/null || useradd -u 4000 -g 4000 -s /bin/bash -c "vitamui user for rsc" vitamui

