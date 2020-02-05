setenforce 0
# FOr next reboot
sed -i 's/^SELINUX=.*/SELINUX=disabled/g'  /etc/selinux/config

