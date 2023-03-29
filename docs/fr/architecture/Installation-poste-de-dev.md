# Installation du poste de développement

!!! info
    Vous retrouverez tous les scripts cités dans cette documentation dans le dossier scripts

## Installation Java

Lancer la commande : sudo sh jdk8u171.sh
Tester sur un nouveau terminal la commande : java -version

L’information suivante doit s’afficher :

```console
java version "1.8.0_171"
Java(TM) SE Runtime Environment (build 1.8.0_171-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.171-b11, mixed mode)
```

## Installation Maven

Lancer la commande :

```console
sudo sh maven.sh
```

Relancer votre terminal pour que l’ajout des binaires maven au PATH soit pris en compte.
Tester sur un nouveau terminal la commande : `mvn --version`

L’information suivante doit s’afficher :

```console
Apache Maven 3.5.2 (138edd61fd100ec658bfa2d307c43b76940a5d7d; 2017-10-18T09:58:13+02:00)
Maven home: /usr/local/apache-maven
Java version: 1.8.0_161, vendor: Oracle Corporation
Java home: /usr/local/jdk1.8.0_161/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.13.0-36-generic", arch: "amd64", family: "unix"
```

Renommer le fichier settings.xml qui se trouve dans /usr/local/apache-maven/conf en settings.xml.old
Mettre le fichier settings.xml qui se trouve dans Poste Dev dans /usr/local/apache-maven/conf
Modifier dans le fichier settings.xml, la balise localRepository pour mettre le dossier correspondant à votre machine.
Installation Eclipse

## Installation Eclipse

Lancer la commande : `sudo sh eclipse.sh`
Tester sur un nouveau terminal la commande : `eclipse`

Eclipse doit se lancer.

Depuis **Eclipse Marketplace**, installer :

* Eclipse Checkstyle 8.8.0
* Eclipse-pmd 1.10
* Spring Tools 3.9.2
* YEdit
* FindBugs : installation depuis <http://findbugs.cs.umd.edu/eclipse/>
* Lombok : télécharger Lombok via <https://projectlombok.org/download> et suivre les instructions depuis <https://projectlombok.org/setup/eclipse>
* PMD : installation en suivant les instructions de  <https://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site/>

A partir du dossier eclipse, installer les différents éléments du formatter.

## Installation Docker3

Lancer la commande : `sudo sh docker.sh`
L’information suivante doit s’afficher :

```console
Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://cloud.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/engine/userguide/
```

## Installation docker-compose

```console
sudo curl -L https://github.com/docker/compose/releases/download/1.19.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

Se connecter au docker VITAMUI avec la commande : sudo docker login `docker.vitamui.com`

## Installation Git

Lancer la commande : `sudo apt-get install git-core`
Tester sur un nouveau terminal la commande : `git --version`
L’information suivante doit s’afficher :

```console
git version 2.14.1
```

## Configuration de Git

Mettre à jour les informations du commiter en remplaçant l’user name et l’user email par vos propres informations.

* Lancer la commande : `git config --global user.name "Makhtar DIAGNE"`
* Lancer la commande : `git config --global user.email "makhtar.diagne@vitamui.com"`

Vérifier la configuration à l’aide de la commande suivante : `git config --list`
Les informations suivantes doivent s’afficher :

```console
user.name=Makhtar DIAGNE
user.email=makhtar.diagne@vitamui.com
```

Définir l’autorebase à la place du merge avec la commande suivante : `git config --global pull.rebase true`
Pour des branches existantes, utiliser la commande pour la branche master par exemple : `git config branch.master.rebase true`

## Bitbucket : Générer une clé SSH

Vous devez disposer d’un compte BitBucket de VITAMUI.

Lancer la commande et mettre les informations suivantes: ssh-keygen
Répertoire où sera stockée la clé : /home/makhtar/.ssh/id_rsa_vitamui
Ne mettez pas de passphrase.

Tester sur un terminal la commande : ls ~/.ssh

L’information suivante doit s’afficher :

```console
id_rsa_vitamui id_rsa_vitamui.pub
```

Copier le contenu de la clé  `~/.ssh/id_rsa_vitamui.pub` et le rajouter dans le compte BitBucket.

Tester sur un terminal la commande : `ssh -T git@bitbucket.org`
L’information suivante doit s’afficher :

```console
logged in as makhtarvitamui.

You can use git or hg to connect to Bitbucket. Shell access is disabled.
```

cf <https://confluence.atlassian.com/bitbucket/set-up-an-ssh-key-728138079.html#SetupanSSHkey-ssh2>

Bitbucket : Récupérer le projet VITAMUI

Lancer la commande: `git clone git@bitbucket.org:vitamui/vitamui.git`
Lancer un build complet Maven avec un `mvn clean install`.

## Installation SublimeText et/ou VIsual Studio Code

Lancer la commande : `sudo sh sublimeText.sh`
SublimeText est installé et peut être lancé depuis le menu Applications -> Programming.

Pour Vsual Studio Code, suivre les instructions ici <https://code.visualstudio.com/docs/setup/linux>

## Installation NodeJS / Npm / AngularCli

Lancer la commande : `sudo sh front.sh`

Lancer sur le terminal la commande : `node -v`

L’information suivante doit s’afficher :

```console
v12.5.0
```

Lancer sur le terminal la commande : `npm -v`
L’information suivante doit s’afficher :

```console
6.9.0
```

Lancer sur le terminal la commande : `ng -version`
L’information suivante doit s’afficher :

```console
Angular CLI: 8.0.6
Node: 12.5.0
OS: linux x64
Angular:
...
```

## MongoDB

Lancer un docker Mongo avec la commande suivante qui se trouve dans vitamui/tools/docker/mongo :
restart_dev.sh

Cette commande permet d’accéder à Mongo sur localhost:27018. Le port 27017 est réservé au Mongo de Vitam
Installer robot3t <https://robomongo.org/download>

## VPN
Il faut obtenir de l’équipe Devops un accès VPN afin de pouvoir attaquer les machines VITAM depuis le poste de Dev.
Il faut ensuite mettre les fichiers de configuration du VPN dans un répertoire openvpn.

créer un dossier openvpn :
```console
mkdir ~/.config/openvpn
```

Y copier les fichiers (à demander à l’équipe Devops) :
ca.crt
vitamuivpn.conf
myvpn.tlsauth
yahya.crt
yahya.key

Lancer la commande de connexion VPN :
```console
sudo openvpn --config vitamuivpn.conf
```

Tester avec un ping sur turtle :
```console
ping 10.0.0.116
```


## Fichier hosts

Pour accéder aux UI sur la machine de Dev, il faut compléter le fichier hosts avec les éléments suivants :

```text
127.0.0.1       dev.vitamui.com
```
