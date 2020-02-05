adduser --home /vitamuiroot --uid 9001 vitamuiroot
echo __VITAMUIROOT_PASSWD__ | passwd vitamuiroot --stdin
echo 'vitamuiroot ALL=(ALL:ALL) ALL' | EDITOR='tee -a' visudo

