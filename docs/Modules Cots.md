# Modules Cots

## Guidelines

Les COTS, software utlises par les solutions VITAMUI et VITAM, sont tous
open-source. Pour des besoins de maintenabilite et de securites, ils
sont entierement repackages au format RPM puis publies sur les
repository yum VITAMUI.

VITAMUI s'appuie sur deux types de COTS dans son architecture:

* les COTS fournit par vitam auxquels VITAMUI se branche.

* les COTS utilises uniquement par VITAMUI

Le packaging des COTS suivront les principes suivants:

* Les noms de package cots seront de la forme vitamui|vitam-COTS_NAME. Les
services systemd installes sur les systemes suivront la meme convention
de nommage

* Les fichiers repackages permettront d'appliquer la protection
de droits users system vitamui,vitam,vitamuidb,vitamdb

* Dans la mesure du possible les packages COTS rpm pourront contenir
l'ensemble des fichiers du software

* Dans le cas contraire, le packages COTS VITAMUI|VITAM contiendrons des
dependances vers les packages RPM officiels. Ils fourniront comme fichiers
l'unit systemd du service COTS et des fichiers de configurations stockes
dans le systeme de fichiers VITAMUI/VITAM permettant de proteger le lancement
du service par les droits users systeme linux.

Le packaging specifique des cots VITAMUI contiendra un Makefile dedie pour
chaque afin d'adapter la generation du contenu des packages. Ils pourront
egalement contenir des templates de packaging dedies pouvant redefinir
les fichiers unit systemd, les fichiers de configurations, les scripts
d'installation RPM executes.

Le repackaging entier des COTS est la technique a priviligier pour
les raisons suivantes:

* l'installation / desinstallation des fichiers pourra se faire
entierement dans les scripts RPM

* aucune dependance RPM donc pas d'etapes d'installation / desinstallation
supplementaire et pas de gestion de repository supplementaire si la
dependance n'est pas dans les repository officiels RedHat ou epel-release.

* Eventuellement, les sources/binaires des cots pourront etre conserves
dans le repository vitamui.


### Liste des cots Vitam

* consul
* mongo*
* mongo-express
* syslog
* elasticsearch
* curator
* siegfried
* cerebro
* logstash
* kibana
* apache

### Liste des cots VITAMUI

* consul
* logstash
* syslog
* mongo*
* nginx


## Packaging des cots VITAMUI

### vitamui-consul

Le soft **consul** sera entierement repackager dans vitamui-consul. Ce
package contiendra

* le binaire consul dans **/vitamui/bin/consul/consul**,
* le fichier unit systemd vitamui-consul.service
* la ligne de commande consul dans **/vitamui/conf/consul/sysconfig**

**Mise a jour de la version de consul:**

La version de consul embarquee dans le package sera parametrable au
niveau du fichier pom.xml du cots vitamui-consul
(fichier **vitamui/cots/vitamui-consul/pom.xml**). Ce nom de version sera transmis
en parametre du makefile qui telechargera le binaire consul dans la
bonne version

```xml
    ...
    <argument>CONSUL_VERSION=1.4.1</argument>
    ...
```

### vitamui-logstash

Le soft **logstash** sera entierement repackage dans vitamui-logstash
a partir de **l'archive des sources Logstash**. Le package RPM
contiendra:

* les librairies Java et fichiers de logstash
* les fichiers de configuration de logstash adapte au file system vitamui
dans /vitamui/conf/logstash
* l'unit systemd vitamui-logstash

Les scripts d'installation RPM appliquerons les droits vitamuidb
au package logstash (a corriger).

**Mise a jour de la version de logstash:**

La version de logstash embarquee dans le package sera parametrable au
niveau du fichier pom.xml du cots vitamui-logstash
(fichier **vitamui/cots/vitamui-logstash/pom.xml**). Ce nom de version sera transmis
en parametre du Makefile qui telechargera l'archive de source logstash
correspondant a la version choisie.


```xml
    ...
    <argument>LOGSTASH_VERSION=7.6.0</argument>
    ...
```


### vitamui-nginx

Le package COTS vitamui-nginx installera le soft **nginx** par le biais d'une
dependance RPM. Cette dependance ira chercher le binaire NGINX depuis
le repository **epel-release**. Le package vitamui-nginx contiendra:

 * l'unit systemd vitamui-nginx
 * la configuration du logrotate de NGINX dans le systeme de fichier
 vitamui

**Mise a jour de la version de NGINX:**

La version **latest** de NGINX sera installee sur le systeme. La mise
a jour du softs s'effectuera a partir d'une update yum.


### vitamui-mongod

Le package COTS vitamui-mongod installera le soft **mongo-org** par le biais d'une
dependance RPM. Cette dependance ira chercher le binaire NGINX depuis
les repository officiels mongo. Le package vitamui-mongod contiendra:

 * l'unit systemd vitamui-nginx
 * la configuration du logrotate de mongo dans le systeme de fichier
 vitamui

La dependance vers mongod-org apportera sur le systeme les softs
mongod, mongoc, mongos, mongodump, mongocli.

**Mise a jour de la version de mongod:**

La version mongo sera parametree au niveau du repository mongod installee
sur le systeme. L'url de ce repository est renseignee dans le deploiement
(var **mongo_repository_url**, https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/{{ mongod_version }}/x86_64/)


* mise a jour mineure de mongo: => yum update via le deploiement (non teste encore
et mongo est installe en version latest sur le systeme).

* mise a jour majeure de mongo: modifier dans le deplioement la variable **mongod_version**
au niveau du fichier **environment/group_vars/all/mongod.yml**.
    * pour un redeploiement from scratch, la version sera prise en compte au niveau de l'installation
    * pour la mise a jour d'un environnement, suivre la procedure de mise a jour mongo


### vitamui-mongo-express

Le package rpm vitamui-mongo-express est entierement repackage a partir de
de l'installation via **npm**. Le package contient toutes les sources
de mongo-express installee dans  /vitamui/app/mongo-express et le ficher
unit systemd de vitamui-mongo-express.

**Mise a jour de la version de mongo-express**

Pour modifier la version de mongo express, editez la dans le fichier
**cots/vitamui-mongo-express/package.json**.

