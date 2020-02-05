# VITAMUI Déploiement

## Pré-requis

 * Installer ansible-2.7.x ou supérieur
 * Configurer le fichier `environment/hosts`
 * Modifier les propriétés dans `environment/group_vars/all/*.yml`

## Installation

Exécuter:

    ./install.sh

Vous pouvez placer en argument du scripts n'importe quels arguments à rajouter à l'appel d'ansible-playbook.

Exemple:
Vous pouvez restreindre l'installation en utilisant les tags `./install.sh --tags <tag>` avec l'un des tags suivants :

 * zone-vitamui-rp
 * zone-vitamui-ui
 * zone-vitamui-app
 * zone-vitamui-data
 * zone-vitamui-infra
 * zone-vitamui-cas


## Désinstallation

Vous pouvez placer en argument du scripts n'importe quels arguments à rajouter à l'appel d'ansible-playbook.

Exécuter:

    ./uninstall.sh


## Deploiement dev - FULL LOCAL

### Full local:

Permet le déploiement de tout l'applicatif VITAMUI à partir du code source.

 * le déploiement est effectué avec les sources du répertoire courant
 * Par défault les RPM de vitamui, préalablement construits, seront exporté sous forme de repository **yum**
 dans chacune des VM locales de déploiement
 * Pour modifier ce comportement il est possible d'installer vitamui à l'aide du repository distant yum vitamui

### Pré-requis:

* vagrant
* virtualbox


### Commandes de gestion des machines virtuelles cibles

* Pour les commandes suivantes, nous pourrons ajouter en parametre le nom d'une VM pour ne l'appliquer que sur une VM.
  Sans argument, la commande s'appliquera à toutes les VM du fichier Vagrgantfile

* L'identifaction des VM se fait dans le fichier **./vagrant/Vagrantfile**. On regardera les sections

    **config.vm.define "VM_NAME_IN_VAGRANTFILE" do |config|**

##### Demarrage et initialisation:

    ./vagrant/start.sh
    ./vagrant/start.sh  [VM]


##### Arret:

    ./vagrant/stop.sh
    ./vagrant/stop.sh [VM]


##### Destruction:

    ./vagrant/purge.sh
    ./vagrant/purge.sh [VM]


##### Reinitialistation:

    ./vagrant/reset.sh
    ./vagrant/reset.sh [VM]


##### Provision (execution des scripts de provisionning de la VM configurés dans le Vagrantfile):
A noter, le provisionning est automatiquement appliqué dans les étapes de start et reset.

    ./vagrant/provision.sh
    ./vagrant/provision.sh [VM]


##### Connection ssh à une VM (VM arg obligatoire)

    ./vagrant/ssh.sh [VM]


##### Consultation du statut:

    ./vagrant/status.sh
    ./vagrant/status.sh [VM]


##### Envoi d'un fichier dans une VM (VM arg obligatoire)

    ./vagrant/push_file.sh [VM_NAME_IN_VAGRANTFILE] [CHEMIN_DU_FICHIER_ENVOYER] [DESTINATION_REMOTE]


##### Snapshot:

Les VM peuvent etre snapshotées. L'idée est de pouvoir se remettre facilement dans un état sans avoir à recréer et
reprovisonner de machine. Un seul snaphost sera conservé et chaque nouvel appel effacera le précédent.

Prise du snapshot:

    ./vagrant/snapshot.sh
    ./vagrant/snapshot.sh [VM_NAME_IN_VAGRANTFILE]

Restauration du snapshot:

    ./vagrant/restore.sh
    ./vagrant/restore.sh [VM_NAME_IN_VAGRANTFILE]

##### Synchronisation du repertoire partagé entre le host et les VM:

Quand vagrant se lance, il va mapper le contenu d'un repertoire du host dans un repertoire de la VM. Par défault,
nous prendrons sur le poste admin des vm le repertoire vitamui/ tout entier et le mapper sur le répertoire /vagrant dans
les VM. La synchronisation automatique de ces repertoires est désactivée. Si un changement du code source est à mettre
à jour à l'interieur des VM, il faudra explicitement appeler la commande suivante;

    ./vagrant/sync.sh
    ./vagrant/sync.sh [VM_NAME_IN_VAGRANTFILE]


### Installation VITAMUI :

* La commande d'installation vagrant va appliquer le playbook d'installation vitamui sur les machines cibles,
en prenant par défault un repository RPM local à la VM.

* Pour un premiere install depuis la CREATION DES VM CIBLES (i.e. apres un **vagrant/start.sh** si aucune VM
créées ou **vagrant/reset.sh**)il faudra ajouter le flag "-c" à la commande pour créer le repository


Pour activer la création du repository préalable à l'installation de vitamui:

    ./install_vagrant.sh -c

Pour utiliser le repository remote à la place du repo local à la VM:

    ./install_vagrant.sh -r

Pour activer la création des entrées dans le fichier hosts du systeme hosts (i.e. la machine du dev par exemple):

    ./install_vagrant.sh -n

Pour préciser une version d'installation:

    ./install_vagrant.sh -v [VITAMUI_RPM_VERSION]

Il est bien entendu possible de combiner tous les flags précédents.


### Desinstallation VITAMUI:

    ./uninstall_vagrant.sh
