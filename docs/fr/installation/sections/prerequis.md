# Prérequis

VitamUI fonctionne avec le socle applicatif Vitam qui doit être préinstallé.

Tout comme Vitam, VitamUI est installé sur des VMs (machines virtuelles) qui doivent être dimensionnées correctement.

Voici le détail de consommation mémoire par défaut des services VitamUI permettant de faire la répartition par VMs.

Les services Java de VitamUI ont pour configuration par défaut `-Xms128m -Xmx512m` ce qui est suffisant pour la majorité des cas d'usage. Cependant, il est possible de surcharger cette configuration par défaut à l'aide des variables `vitamui.<composant>.jvm_opts.memory` du fichier `environments/group_vars/all/jvm_opts.yml`.

De même, pour la base mongo-vitamui, une limitation de `mongod_memory=1` est suffisant vu la quantité de données stockée dans cette base (sans cette limitation, par défaut, la base mongo-vitamui consommera la moitié de la RAM disponible).

Ainsi, on peut envisager la répartition suivante tout en assurant un minimum de redondances des composants:

| Server | VCPUs | RAM  | Composants                                                   | Total RAM |
|--------|:-----:|:----:|--------------------------------------------------------------|:---------:|
| UI-1   | 2     | 4    | ReverseProxy<br/>  Tous les composants UIs                   | < 1Go     |
| UI-2   | 2     | 4    | ReverseProxy<br/>  Tous les composants UIs                   | < 1Go     |
| APP-1  | 4     | 8    | mongo-vitamui (1G)<br/>api-gateway (128m – 512m)<br/>iam (128m – 512m)<br/>ingest (128m – 512m)<br/>referential (128m – 512m)<br/>archive_search (128m – 512m)<br/>pastis (128m – 512m) | < 4Go    |
| APP-2  | 4     | 8    | mongo-vitamui (1G)<br/>api-gateway (128m – 512m)<br/>iam (128m – 512m)<br/>ingest (128m – 512m)<br/>collect (128m – 512m)<br/>referential (128m – 512m)<br/>security (128m – 512m) | < 4Go    |
| APP-3  | 4     | 8    | mongo-vitamui (1G)<br/>cas-server (128m – 512m)<br/>collect (128m – 512m)<br/>pastis (128m – 512m)<br/>archive_search (128m – 512m)<br/>security (128m – 512m) | < 4Go    |

Les composants suivants sont optionnels:

* consul
* logstash
