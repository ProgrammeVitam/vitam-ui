
## Certificats et PKI

La PKI permet de gérer de manière robuste les certificats de la solution VITAMUI. Une PKI est une architecture de confiance constituée d’un ensemble de systèmes fournissant des services permettant la gestion des cycles de vie des certificats numériques :

* émission de certificats à des entités préalablement authentifiées
* déploiement des certificats
* révocation des certificats
* établir, publier et respecter des pratiques de certification de confiance pour établir un espace de confiance

### Principes de fonctionnement PKI de VITAMUI

La PKI VITAMUI gère les certificats nécessaires à l'authentification des services VITAMUI et des entités extérieurs. La logique de fonctionnement de la PKI VITAMUI est similaire à celle utilisée par la solution VITAM. 

Les principes de fonctionnement de la PKI sont les suivants :

* Emission des certificats VITAMUI (les dates de création et de fin de validité des CA sont générées dans cette phase).
* Gestion du cycle de vie (révocation) des certificats
* Publication des certificats et des clés (.crt et .key)
* Déploiement :
    * Génération des magasins de certificats VITAMUI (les certificats .crt et .key sont utilisés pour construire un magasin de certificats qui contient des certificats  .p12 et .jks) 
    * Déploiement dans VITAMUI des certificats .p12 et .jks par Ansible


    Schéma de la PKI :
  
  ![PKI](../images/dat_pki_1.png)  

### PKI de test

VITAMUI propose de générer à partir d’une PKI de tests les autorités de certification root et intermédiaires pour les clients et les serveurs. Cette PKI de test permet de connaître facilement l’ensemble des certificats nécessaires au bon fonctionnement de la solution. Attention, la PKI de test ne doit être utilisée que pour faire des tests, et ne doit surtout pas être utilisée en environnement de production. 

### Liste des certificats utilisés

Le tableau ci-dessous détail l’ensemble du contenu des keystores et truststores par service.

|Composants  |  Keystores  |Truststores|
|------------|-------------|-----------|
|**ui-portal**  |  ui-portal.crt, ui-portal.key  | ca-root.crt, ca-intermediate.crt |
|**ui-identity**  |  ui-identity.crt, ui-identity.key  | ca-root.crt, ca-intermediate.crt |
|**ui-identity-admin**  |  ui-identity-admin.crt, ui-identity-admin.key  | ca-root.crt, ca-intermediate.crt |
|**cas-server**  |  cas-server.crt, cas-server.key  | ca-root.crt, ca-intermediate.crt |
|**iam-external**  |  iam-external.crt, iam-external.key  | ca-root.crt |
|**iam-internal**  |  iam-internal.crt, iam-internal.key  | ca-root.crt |
|**security-server**  |  security-server.crt, security-server | ca-root.crt |

La liste des certificats utilisées par VITAM est décrite à cette adresse : http://www.programmevitam.fr/ressources/DocCourante/html/archi/securite/20-certificates.html 

### Génération des certificats 

La procédure de génération des certificats VITAMUI est la suivante : 

* Génération des certificats d’autorité (CA) : 
    * exécuter le script : 
    ```console 
    ./pki/scripts/generate_ca.sh
    ```

* Génération des certificats : 
    * exécuter le script :
    ```console 
    ./pki/scripts/generate_certs.sh <inventaire>
    ```
Le certificat client VITAMUI est généré dans le dossier deployment/environment/certs/client-vitam.  Lors de la création des keystores, le certificat est ajouté dans les keystores des différents services.

### Configuration du client VITAMUI vers VITAM

Dans VITAMUI, il est nécessaire de configurer une connexion cliente pour pouvoir accéder aux services external du socle VITAM. A cet effet, le certificat client VITAMUI doit être installé dans l'environnement VITAM. De manière réciproque, l'autorité de certification de VITAM doit être connue dans VITAMUI.

Les PKI VITAMUI et VITAM sont très proches d’un point de vue structurel. La synchronisation est donc relativement simple.

* VITAMUI vers VITAM :

    * Copier les CA du certificat VITAMUI afin de les ajouter dans les truststores des APIs externals VITAM : vitamui/deployment/environment/certs/client-vitam/ca → vitam/deployment/environment/certs/clients-external/ca 

    * Copier le certificat VITAMUI afin de les ajouter dans la liste des certificats autorités à utiliser les APIs externals VITAM : vitamui/deployment/environment/certs/client-vitam/clients/vitamui →  vitam/deployment/environment/certs/clients-external/clients/external 

* VITAM vers VITAMUI :

    * Copier les CA de VITAM afin de les ajouter dans le truststore utilisé par le client VITAM dans VITAMUI : vitam/deployment/environment/certs/server/ca → vitam/deployment/environment/certs/clients-vitam/ca

### Génération des stores

Pour générer les stores de VITAMUI, il faut exécuter le script : 

 ```console 
    ./generate_stores.sh <inventaire> 
```

### Procédure d’ajout d’un certificat client externe

Le certificat ou l’autorité de certification doit présent dans les truststores des APIs external VITAMUI. La procédure d'ajout d’un certificat client externe aux truststores des services de VITAMUI est la suivante :

* Déposer le(s) CA(s) du client dans le répertoire deployment/environment/certs/client-external/ca

* Déposer le certificat du client dans le répertoire deployment/environment/certs/client-external/clients/external/

* Régénérer les keystores à l’aide du script deployment/generate_stores.sh

* Exécuter le playbook pour redéployer les keystores sur la solution VITAMUI :  

```console 
ansible-playbook vitamui_apps.yml -i environments/hosts --vault-password-file vault_pass.txt --tags update_vitamui_certificates
```

L’utilisation d’un certificat client sur les environnements VITAMUI nécessite également de vérifier que le certificat soit présent dans la base de données VITAMUI et rattaché à un contexte de sécurité du client.