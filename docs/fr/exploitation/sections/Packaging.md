# Packaging

## Packaging JAVA

!!! todo
    Ecrire la doc sur le packaging Java

```bash
mvn clean package
```

## Packaging Javascript

!!! todo
    Ecrire la doc sur le packaging Javascript

```bash
mvn clean package -P webpack
```


## Packaging deployment - ansible

* Le source code ansible contenu dans le repertoire deployement sera
package sous forme d'archive tar.gz.

* Cette archive sera versionnee et publiee comme un artifact Maven
dans le repository maven private de VITAMUI.

* Pour l'utiliser, il faudra la telecharger depuis le repo et
reconfigurer les fichiers souhaites pour adapter la configuration par
defaut a son environnement.

## Packaging RPM

Un fichier RPM est un package utilisable par les repository des
systemes RedHat.

Pour VITAMUI, Il contient:

* L'arboresence de repertoire du system de fichier vitamui pour
l'applicatif en question

* Des binaires a installer sur le systeme cible. Dans le cas des
applicatifs VITAMUI, il s'agira des JAR files cree dans la phase de
packaging du compilateur maven.

* Des fichiers de configuration par defaut

* Des metadonnnees sur le package (version, license, nom ...)

* Des scripts execute avant l'installation, apres  l'installation,
avant la desinstallation et apres la desinstallation. Ces scripts
se chargeront de modifier les permissions/ownership des fichiers et dossiers
installes par le rpm. Ils effectuerons egalement l'enregistrement(/suppression) des services
dans systemd a l'installation(/desinstallation) des packages RPM.


### Tools de packaging VITAMUI

Ils se trouvent dans le repertoire tools/packaging. On y trouve:

* **install_fpm.sh**:  install l'outil FPM psur le systeme

!!! note
    FPM est un outil de package permettant de gerer tout type de package. Il est developpe (ruby) et maintenu
    par Jordan Sissel, lead du projet Logstash. Il serait cependant judicieux de remplacer cet outil par l'outil par
    la creation d'un fichier RPMSpec et rpmbuild pour creer les packages.
    Ex: https://doc.fedora-fr.org/wiki/La_cr%C3%A9ation_de_RPM_pour_les_nuls_:_Cr%C3%A9ation_du_fichier_SPEC_et_du_Paquetage


* **Makefile**: Makefile par default execute pour toutes les applications developpees par VITAMUI

* **publish.sh**: Script de publication d'un RPM sur le repository yum vitamui

* **templates/**: Repertoire contenant les templates de scripts RPM et unit systemd, appliques pour chaque application
VITAMUI



### Fabrication des RPM

La fabrication des paquets RPM se fait en plusieurs etapes:

* Creation de l'arboresence des application VITAMUI dans un staging directory

* Processing des templates (scripts d'installation / unit systemd / fichier de config command line java)

* Installation des binaires, fichiers et fichier templates dans l'arborescence VITAMUI ainsi que du fichier unit systemd

* packaging en fichier RPM du staging directory avec les scripts RPM et les metadonnees du package.


Ces etapes se font en appelant, dans le repertoire **target/**, le Makefile de packaging de la sorte:

```bash
make -f [VITAMUI PROJECT DIR]/tools/packaging/Makefile  rpm  NAME=[APPLICATION NAME] VERSION=[BUILD_VERSION]  JAR_FILE=[PATH TO BUILD JAR FILE]  USER=[USER THAT WILL RUN VITAMUI SERVICE] DEPENDENCIES=[COMMA SEP LIST OF RPM DEPENDENCIES]
```

Pour mieux comprendre ce qu'il se passe dans cette comande, voici le detail des operations effectuees.


#### Contenu du RPM : Makefile Staging directory

Le staging directory est le pattern utilise pour creer les package VITAMUI. Il consiste a installer dans un repertoire de
build l'arborescence de repertoires + fichiers comme elle seraint installée a la racine du serveur

```bash
package-stage/
├── vitamui
│   ├── app
│   │   └── archive-internal
│   ├── bin
│   │   └── archive-internal
│   ├── conf
│   │   └── archive-internal
│   ├── data
│   │   └── archive-internal
│   ├── defaults
│   │   └── archive-internal
│   ├── lib
│   │   └── archive-internal
│   ├── log
│   │   └── archive-internal
│   ├── run
│   │   └── archive-internal
│   ├── script
│   │   └── archive-internal
│   └── tmp
│       └── archive-internal

```

Pour builder uniquement cet arborescence:

```
make -f [VITAMUI PROJECT DIR]/tools/packaging/Makefile  vitamui-dirs   NAME=[APPLICATION NAME] VERSION=[BUILD_VERSION]  JAR_FILE=[PATH TO BUILD JAR FILE]  USER=[USER THAT WILL RUN VITAMUI SERVICE] DEPENDENCIES=[COMMA SEP LIST OF RPM DEPENDENCIES]
```


#### Creation des templates du RPM

Le makefile va creer dans le repertoire **target/package-template/** les scripts d'installation, les fichiers de
configurations ainsi que le fichier unit systemd pour l'application en question.

Nous noterons qu'il est possible de rajouter ou modifier des templates par default pour une application. Pour cela, il
suffit de creer dans le repertoire de l'application un repertoire packaging avec les fichiers a ajouter
ou modifier. C'est le cas pour l'application cas-server

Pour generer ces templates:

```
make -f [VITAMUI PROJECT DIR]/tools/packaging/Makefile  template-files   NAME=[APPLICATION NAME] VERSION=[BUILD_VERSION]  JAR_FILE=[PATH TO BUILD JAR FILE]  USER=[USER THAT WILL RUN VITAMUI SERVICE] DEPENDENCIES=[COMMA SEP LIST OF RPM DEPENDENCIES]
```

#### Installation des binaires et fichiers templates dans le stating directory:

C'est ce qui est fait en appelant la cible **install** du Makefile.  Le makefile va copier JAR_FILE dans le repertorie
vitamui/app/APP_NAME/ puis ensuite copier les fichiers de configuration vitamui/app/APP_NAME/sysconfig/cmd_line_args
vitamui/app/APP_NAME/sysconfig/cmd_line_args. Enfin, il copiera dans usr/lib/systemd/systemd  le fichier
vitamui-APP_NAME.service gerant l'unit systemd de l'application

```
make -f [VITAMUI PROJECT DIR]/tools/packaging/Makefile  install NAME=[APPLICATION NAME] VERSION=[BUILD_VERSION]  JAR_FILE=[PATH TO BUILD JAR FILE]  USER=[USER THAT WILL RUN VITAMUI SERVICE] DEPENDENCIES=[COMMA SEP LIST OF RPM DEPENDENCIES]
```


Resultat:

```bash
package-stage/
├── vitamui
│   ├── app
│   │   └── archive-internal
│   │       └── archive-internal-develop.jar
│   ├── bin
│   │   └── archive-internal
│   ├── conf
│   │   └── archive-internal
│   │       └── sysconfig
│   │           ├── cmd_line_args
│   │           └── java_opts
│   ├── data
│   │   └── archive-internal
│   ├── defaults
│   │   └── archive-internal
│   ├── lib
│   │   └── archive-internal
│   ├── log
│   │   └── archive-internal
│   ├── run
│   │   └── archive-internal
│   ├── script
│   │   └── archive-internal
│   └── tmp
│       └── archive-internal
└── usr
    └── lib
        └── systemd
            └── system
                └── vitamui-archive-internal.service

```


#### packaging du repertoire

Le packaging se fait avec fpm. Un exemple de packaging d'un source directory vers RPM (avec staging to /) :

```bash
    fpm -s dir [FPM_OPTIONS] --before-install "before-install.sh" --after-install after-install.sh -t rpm -p [PACKAGE_PATH]  "[STAGING_ROOT]/=/"
```

Package creation options

* -s dir:                 indique un repertoire comme source de packaging
* -t rpm:                 format de sortie rpm
* [STAGING_ROOT]/=/       mapping du root path dans le rpm

Dans les FPM_OPTIONS, nous aurons notammment:

*  -d : la liste des dependances RPm
* --description: description du package. Nous y integrerons par exemple commit id
*  --licence: la license du package
* -v : version du pakage
* les scripts RPM:  --before-install "$(TEMPLATE_TMP_DIR)/before-install.sh" --after-install "$(TEMPLATE_TMP_DIR)/after-install.sh"
= --before-remove "$(TEMPLATE_TMP_DIR)/before-remove.sh" --after-remove "$(TEMPLATE_TMP_DIR)/after-remove.sh"



Cette etape se fait en appelant la target **rpm** du Makefile.


### Integration dans maven

* Le packaging des RPM est effecutuee dans maven en ajoutant le profil "rpm" lors de l'appel de build.

* le profil RPM est decrit dans le fichier **pom.xml** general du projet

* le packaging Rpm est effectue à la phase "package" de maven

```bash
mvn clean package -P rpm
```



### How to


Liste d'operations utiles sur les rpm


* Telecharger un fichier RPM depuis un repository YUM


```
# Installer :
yum install yum-utils -y

# Pour downloader le package:
yumdownloader [PACKAGE_NAME]
```

* Installer un rpm avec ses dependances depuis un fichiers

```
yum install [PATH_TO_FILE]
```

* Extraire un fichier rpm sous ubuntu:

```bash
rpm2cpio [RPM_FILE] | cpio -idmv
```

* Voir les scripts d'installation du package:

```bash
rpm -qp --scripts [RPM_FILE]
```
