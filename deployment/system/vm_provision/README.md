# Etapes de provisonning system

Les scripts fournis a coté peuvent tres bien etre saisis interactivement dans la console VNC

## Update systems

script update_system.sh
=> Peut etre fait avec le provisonner local_exec


## Installer les paquets de tools

script setup_system_tools.sh
=> Peut etre fait avec le provisonner local_exec


## Desactiver SELinux

script deactivate_selinux.sh
=> Peut etre fait avec le provisonner local_exec


## Creer un user vitamuiroot et ajout ses droits sudoers (toutes commandes permises avec saisie de mot de passe

Generer un mot de passe pour l'utilisateur vitamuiroot; note: c'est ce mot de passe qui sera demande lors des appels sudo

script create_user.sh
=> Peut etre fait avec le provisonner local_exec

## Assurer la config ssh

Il faut s'assurer que le serveur ssh:

 - permette l'authentification par clé ssh (option "PubkeyAuthentication yes","GSSAPIAuthentication yes")
 - aille chercher les clé publique dans les fichiers authorized_keys des repertoire $HOME/.ssh/authorized_keys ("AuthorizedKeysFile      .ssh/authorized_keys")
 - n'essaye pas de faire la resolution DNS inverse de l'IP du client (option: "UseDNS no")

Soit editer le fichier /etc/ssh/sshd_config a la main, soit utiliser le script

Version script config_ssh_server.sh
=> Peut etre fait avec le provisonner local_exec

Puis restarter le serveur ssh (peut etre fait avec le provisonner local_exec)

systemctl restart sshd.service


## Creer une paire de clé ssh.

Sur ton pc, genere la clé ssh que tu fournira

ssh-keygen -f ./vitamuiroot_rsa -q -N ""

## Envoyer la clé publique à l'utilisateur vitamuiroot

Copier le contenu du fichier ./vitamuipour root_rsa.pub dans le fichier /vitamuiroot/.ssh/authorized_keys du serveur.
Une cle par ligne

C'est la que ca se complique, mieux vaut passer d'abord par le ssh avec password parce qu'interactivement c'est pas
possible de saisir cette clé :P


## Tester la connection ssh:

ssh -i [CHEMIN_VERS]/vitamuiroot_rsa vitamuiroot@[IP_DE_LA_MACHINE]


