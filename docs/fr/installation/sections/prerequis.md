# Prérequis

Vitam-UI fonctionne avec le socle applicatif Vitam qui doit être préinstallé.

Tout comme Vitam, Vitam-UI est installé sur des vms (machines virtuelles) qui doivent être dimensionnées correctement.

Voici le détail de consommation mémoire par défaut des services Vitamui, ce qui va permettre de faire la répartition par vm(s).

Par défaut les services java utilisent 512Mo de Ram et on a donc pour tous les services en présence:

* cas-server                512Mo
* consul ? (à voir)
* mongod ? (à voir)
* logstash :  -Xms{{ (ansible_memory_mb.real.total / 8) | int }}m  => total Ram systeme. -Xmx{{ (ansible_memory_mb.real.total / 4) | int }}m
* rsyslog ? (à voir)
* security-internal			512Mo
* iam-external				512Mo
* iam-internal				512Mo
* archive-search-external	512Mo
* archive-search-internal	512Mo
* ui-archive-search			512Mo
* ingest-internal			512Mo
* ingest-external			512Mo
* ui-ingest					512Mo
* referential-internal		512Mo
* referential-external		512Mo
* ui-referential			512Mo
* ui-identity				512Mo
* ui-identity-admin  		512Mo
* ui-portal			    	512Mo

NB: Ce paramétrage peut être modifié selon les besoins. Des variables sont prévues à cet effet (variable jvm_opts à utiliser pour chaque service).

Exemple de répartition sur 2 hosts (machines virtuelles) disposant de

* 50Go de disque dur
* 8Go de RAM
* 2 VCPU

HOST1:

* (browser)
* archive_search_external
* archive_search_internal
* security_internal
* referential_external
* referential_internal
* ingest_external
* ingest_internal
* iam_external
* iam_internal
* mongod

HOST2:

* (browser)
* cas-server
* ui-identity-admin
* ui-identity
* ui-portal
* ui-ingest
* ui-referential
* ui-archive-search]
* consul_server
* logstash
* reverseproxy

Prequis logiciel:

Sous Centos :

Sous /etc/yum.repo.d/

* CentOS-Base.repo
* CentOS-CR.repo
* CentOS-Debuginfo.repo
* CentOS-fasttrack.repo
* CentOS-Sources.repo
* CentOS-Vault.repo
* CentOS-x86_64-kernel.repo
* epel.repo
* epel-testing.repo

Les paquets de type "CentOS" sont standard à la distribution, les paquets "epel" sont nécessaires à l'installation des binaires "npm" et "nodjs" nécessaires à l'utilisation de "mongo-express".

Sous Debian:

S'assurer que nodejs est installé.
