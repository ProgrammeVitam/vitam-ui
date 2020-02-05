# Activate rsa pubkey auth
grep -q '^PubkeyAuthentication.*' /etc/ssh/sshd_config && sed -i 's/^PubkeyAuthentication.*/PubkeyAuthentication yes/g'  /etc/ssh/sshd_config || echo "PubkeyAuthentication yes" >> /etc/ssh/sshd_config
sed -i 's/^GSSAPIAuthentication.*/GSSAPIAuthentication yes/g'  /etc/ssh/sshd_config || echo "GSSAPIAuthentication yes"  >> /etc/ssh/sshd_config
# Ensure pubkey are looked up in user $HOME/.ssh/authrorized_keys files
sed -i 's/^AuthorizedKeysFile.*/AuthorizedKeysFile      .ssh\/authorized_keys/g'  /etc/ssh/sshd_config || echo "AuthorizedKeysFile      .ssh/authorized_keys"  >> /etc/ssh/sshd_config
# FOr speed, disable DNS use when server tries to check client dns
sed -i 's/^UseDNS.*/UseDNS no/g'  /etc/ssh/sshd_config || echo "UseDNS no"  >> /etc/ssh/sshd_config
