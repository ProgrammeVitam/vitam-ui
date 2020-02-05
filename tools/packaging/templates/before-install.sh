# Init vitamui system groups
getent group  vitamui >/dev/null || groupadd -g 4000 vitamui
getent group  vitamuidb >/dev/null || groupadd -g 5000 vitamui
getent group  vitamuidb-admin >/dev/null || groupadd -g 5001 vitamuidb-admin

# Init vitamui system users
getent passwd vitamui >/dev/null || useradd -u 4000 -g 4000 -s /bin/bash -c "vitamui service user" vitamui
getent passwd vitamuidb >/dev/null || useradd -u 4001 -g 4000 -s /bin/bash -c "vitamuidb database user" vitamuidb
