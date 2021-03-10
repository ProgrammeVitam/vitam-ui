# Modèle de données
## Liste des bases

    iam
    security
    cas

## Base IAM

##### Collections
    customers
    events
    groups
    externalParameters
    owners
    profiles
    providers
    subrogations
    tenants
    tokens
    users


   * _Collection Customer_


| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id  | String     | Clé Primaire  |   |
| identifier    |   String  | minimum = 1, maximum = 12   |   |
| code | String    | minimum = 6, maximum = 20  |   |
| companyName    |   String  | maximum = 250  |   |
| language    |   String  | Non null, valeurs = [FRENCH,ENGLISH]|   |
| passwordRevocationDelay    |   Integer  |  Non null |  exprimé en jour |
| otp    |   Enum  |  Non null, valeurs = [OPTIONAL,DISABLED,MANDATORY] |   |
| emailDomains    | List<_String_>    |  Non null, Non vide |   |
| defaultEmailDomain    |   String  | Non null  |   |
| address    |   Address  |  Non null |   |
| name    |   String  | maximum = 100  |   |
| subrogeable    |  boolean   |  default=false |   |
| readonly    |  boolean   | default=false  |   |
| graphicIdentity    |  GraphicIdentity   |  |   |
|gdprAlert| true,
| gdprAlert    |  boolean   | default=false  |   |
| gdprAlertDelay    |  int   | minimum=1  |   |

   * GraphicIdentity (Embarqué)

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| hasCustomGraphicIdentity | boolean     |   |   |
| logoDataBase64 | String    |   |   |
| logoHeaderBase64 | String    |   | Base64 encoded logo  |
| portalTitle | String    |   |   |
| portalMessage | String    |  maximum length = 500 chars) |   |
| themeColors    |   Map<String, String>   |   |  |
       
   * themeColors

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| vitamui-primary | String     |  hexadeciaml color like |   |
| vitamui-secondary | String     |  hexadeciaml color like |   |
| vitamui-tertiary | String     |  hexadeciaml color like |   |
| vitamui-header-footer | String     | hexadeciaml color like  |   |
| vitamui-background | String     | hexadeciaml color like  |   |
        
   * _Collection Tenants_

Le tenant correspond à un container (ie. espace de travail) logique.
Chaque tenant est unique dans le système et appartient à un seul et unique client.
Un client peut posséder plusieurs tenants.
Un client ne doit jamais pouvoir accéder au tenant d’un autre client.
Les tenants VITAMUI correspondent aux tenants VITAM.
Toutes les requêtes HTTP dans VITAMUI doivent renseigner le tenant dans le header.
Dans VITAMUI, le tenant permet de vérifier les autorisations applicatives (certificat et contexte) et utilisateurs (profils).

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id  | String     | Clé Primaire  |   |
| customerId | String    | Non null, Clé Étrangère  |   |
| identifier    |   Integer  | Non null  |  correspond au tenant vitam |
| ownerId    |   String  | Non null, Clé Étrangère|   |
| name    |   String  |  maximum = 100 |  exprimé en jour |
| proof    |   Boolean  |  |  identifie le tenant de preuve |
| readonly    | Boolean    |   |   |
| ingestContractHoldingIdentifier    |   String  | Non null  | contrat d’entrée pour l’arbre  |
| accessContractHoldingIdentifier    |   String  |  Non null |  contrat d’accès pour l’arbre |
| itemIngestContractIdentifier    |   String  |  Non null |  contrat d’entrée pour les bordereaux |
| accessContractLogbookIdentifier    |   String  |  Non null |  contrat d’accès pour le logbook |
| enabled    |   Boolean  | Non null  |   |

   * _Collection Owner_

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id  | String     | Clé Primaire  |   |
| identifier | String    | minimum = 1, maximum = 12   |   |
| customerId    |   String  | Clé Étrangère  | Clé Étrangère  |
| name    |   String  | maximum = 100|   |
| code    |   String  |  minimum = 6, maximum = 20 |  |
| companyName    |   String  | maximum = 250 |   |
| address    | Address    |   | embedded  |
| readonly    |   Boolean  |   |   |


   * Address (Embarqué)

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| street | String     | maximum = 250  |   |
| zipCode | String    |  maximum = 10 |   |
| city    |   String  | maximum = 100  |  |
| country    |   String  | maximum = 50|   |


 * _Collection Identity Provider_

 L’identity provider L’IDP est soit externe (Clients/Organisations externes) soit interne.
 L’IDP interne est CAS lui même et les utilisateurs sont alors gérés uniquement dans l’annuaire CAS de VITAMUI.


| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| customerId | String    |  Clé Étrangère |   |
| identifier    |   String  |  minimum = 1, maximum = 12  |  |
| name    |   String  | maximum = 100|   |
| technicalName    |   String  | |   |
| internal    |   Boolean  | default=true|   |
| patterns    |   List<_String_>  | minimum = 1|   |
| enabled    |   Boolean  | default=true|   |
| keystoreBase64    |   String  | |   |
| keystorePassword    |   String  | |  Mot de passe |
| privateKeyPassword    |   String  | | Mot de passe  |
| idpMetadata    |   String  | |  XML |
| spMetadata    |   String  | | XML  |
| maximumAuthenticationLifetime    |   Integer  | |   |
| readonly    |   Boolean  | |   |


* _Collection User_

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| customerId | String    |  Clé Étrangère |   |
| enabled    |   boolean  |  default = true |  |
| status    |   Enum  | default = ENABLED, BLOCKED, ANONYM, DISABLED |   |
| type    |   Enum  | NOMINATIVE, GENERIC |   |
| password    |   String  | maximum = 100 | Mot de passe  |
| oldPasswords    |   List<_String_>  |  |   |
| identifier    |   String  |  minimum = 1, maximum = 12  |  |
| email    |   String  | email, Unique |   |
| firstname    |   String  | maximum = 50 |   |
| lastname    |   String  | maximum = 50 |   |
| language    |   String  | Non null, valeurs = [FRENCH,ENGLISH] |   |
| phone    |   String  | phone number |  |
| mobile    |   String  | mobile phone number |   |
| otp    |   Boolean  | default = false |   |
| groupId    |   String  | Not null |   |
| subrogeable    |   Boolean  | |   |
| lastConnection    |   OffsetDateTime  | |   |
| nbFailedAttempts    |   int  | |   |
| readonly    |   boolean  | default=false |   |
| level    |   String  | Not null |   |
| passwordExpirationDate    |   OffsetDateTime  |  |   |
| address    |   Address  |  |   |
| analytics    |   AnalyticsDto  |  |   |


   * AnalyticsDto (Embarqué)

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| applications | ApplicationAnalyticsDto     |  |   |
| lastTenantIdentifier | Integer    |  |   |

   * ApplicationAnalyticsDto (Embarqué)

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| applicationId | String     |  |   |
| accessCounter | int    |  |   |
| lastAccess | OffsetDateTime    |  |  ex: YYYY-MM-ddTHH:mm:ss.ssssssZ |


* _Collection externalParameters

La collection qui définit un contrat d'accès par défaut

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| identifier    |   String  |  minimum = 1, maximum = 12  |  |
| name    |   String  | minimum = 2, maximum = 100|   |
| parameters    |   ParameterDto  |  Not Null |   |

   * ParameterDto (Embarqué)

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| key | String     |  |  exemple: PARAM_ACCESS_CONTRACT |
| value | String    |  | exemple: AC-000001  |

* _Collection Groups_

Le groupe de profil définit un ensemble de profils.
Un groupe de profil ne peut contenir qu’un seul profil par “app:tenant”. Par exemple : “profil(app1:tenant1), profil(app1:tenant2), profil(app2:tenant1)” est autorisé.

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| identifier    |   String  |  minimum = 1, maximum = 12  |  |
| customerId    |   String  |  Non Null, Clé Étrangère |  |
| name    |   String  | maximum = 100 |   |
| description    |   String  | maximum = 250 |   |
| profileIds    |   List<_String_>  | clé étrangére | les profils  |
| level    |   String  | maximum = 250 |   |
| readonly    |   Boolean  |  |   |
| enabled    |   Boolean  | |   |

* _Collection Profils_

Le profil définit les permissions (rôles) données à un utilisateur et l’accès à une application (applicationName), généralement une IHM qui regroupe un ensemble de fonctionnalités selon une logique métier et appelant des API backoffice.
Un profil appartient à une groupe (de profils). Il ne peut y avoir qu’un seule et unique profile par tenant, applicationName dans un groupe.

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| identifier    |   String  |  minimum = 1, maximum = 12  |  |
| tenantIdentifier    |   Integer  |  |  |
| name    |   String  | maximum = 100 |   |
| enabled    |   boolean  | default=true |   |
| description    |   String  | maximum = 250 |
| applicationName    |   String  | maximum = 250 |   |
| roles    |   List<_Role_>  |  | rôle Spring  |
| readonly    |   Boolean  | |   |
| level    |   String  | maximum = 250 |   |
| externalParamId    |   String  |  |   |

* _Collection subrogations_

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| status | Enum    |  CREATED, ACCEPTED |   |
| date    |  Date   |   |  |
| surrogate    |  String   | email, minimum = 4, maximum = 100 |  celui qui est subrogé |
| superUser    |   String  | email, minimum = 4, maximum = 100 |  celui qui subroge |
| surrogateCustomerId    |   String  | not null |   |
| superUserCustomerId    |   String  | not null |  |

* _Collection tokens_

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     |   |   |
| updatedDate | Date    |  not null |   |
| refId    |  String   | not null |  |
| surrogation    |  Boolean   |  |   |

* _Collection Events_

| Nom    | Type | Contrainte(s) | Remarque(s) |
| -------- | -------- | ------ | ------  |
| _id | String     | Clé Primaire  |   |
| tenantIdentifier |   Integer  |  not null |   |
| accessContractLogbookIdentifier    |  String   | not null |  |
| evParentId    |   String  |  |   |
| evIdProc    |   String  |  |   |
| evType    |   String  | not null |   |
| evTypeProc    |  Enum   | EXTERNAL_LOGBOOK |
| outcome    |  Enum   | UNKNOWN, STARTED, ALREADY_EXECUTED, OK, WARNING, KO, FATAL |   |
| outMessg    |   String  | not null |   |
| outDetail    |  String   | not null |   |
| evIdReq    |   String  | not null |   |
| evDateTime    |   String  | not null |   |
| obId    |  String   | not null |   |
| obIdReq    |   String  | not null |   |
| evDetData    |  String   | not null |   |
| evIdAppSession    |   String  | not null |   |
| creationDate    |   Long  | not null |   |
| status    |  Enum   | CREATED, SUCCESS, ERROR |   |
| vitamResponse    |  String   | |   |
| synchronizedVitamDate    |   OffsetDateTime  | |   |

Pour aller plus loin, le modèle de données Vitam concernant les journaux d'archives est accessible par [ici](http://www.programmevitam.fr/ressources/DocCourante/autres/fonctionnel/VITAM_Modele_de_donnees.pdf#%5B%7B%22num%22%3A45%2C%22gen%22%3A0%7D%2C%7B%22name%22%3A%22XYZ%22%7D%2C56.7%2C748.3%2C0%5D)
## Base security

* _Collection Context_

Le contexte applicatif permet d’attribuer à une application cliente selon son certificat X509 transmis lors de la connexion https les droits d’accès (rôles)  à différents services.

  | Nom    | Type | Contrainte(s) | Remarque(s) |
  | -------- | -------- | ------ | ------  |
  | _id | String     |  Clé Primaire |   |
  | fullAccess | Boolean    | default = false  |   |
  | name | String    | not null  |   |
  | tenants    |  List<_Integer_>   | Not Null, List de Clé Étrangère | Liste des tenants autorisés |
  | roleNames    |  List<_String_>   | Not Null |  Liste des rôles autorisés |

* _Collection Certificate_

La collection certificat permet de stocker les certificats correspondant à un contexte.
Le certificat est  transmis par l’application client lors de la connexion SSL.

  | Nom    | Type | Contrainte(s) | Remarque(s) |
  | -------- | -------- | ------ | ------  |
  | _id | String     |  Clé Primaire |   |
  | contextId | String    | Not Null  |   |
  | serialNumber    |  String   | Not Null | Numéro de série du certificat |
  | subjectDN    |  String   | Not Null |  Identifiant unique (Distinguished Name) du certificat |
  | issuerDN    |  String   | Not Null |  Identifiant unique (Distinguished Name) de l’autorité de certification |
  | data    |  String   | Not Null |  Certificat en base64 |

* _Collection CustomSequence_

La collection sequence permet de stocker les différentes séquences utilisés.

  | Nom    | Type | Contrainte(s) | Remarque(s) |
  | -------- | -------- | ------ | ------  |
  | _id | String     |  Clé Primaire |   |
  | name | String    |  Not Null | Nom de la séquence   |
  | sequence    |  int   |  | Valeur courante |

La liste des noms de séquences :

- tenant_identifier
- user_identifier
- profile_identifier
- group_identifier	
- provider_identifier
- customer_identifier
- owner_identifier
	
## Base Cas

Cette base est initialisée à la création de l'environnement. Elle est uniquement utilisée par CAS en lecture seule.

  | Nom    | Type | Contrainte(s) | Remarque(s) |
  | -------- | -------- | ------ | ------  |
  | _id | String     |  Clé Primaire |   |
  | serviceId | String    | Not Null  |  url du service web |
  | name    |  String   |  | nom du service |
  | logoutType    |  String   |  |   |
  | logoutUrl    |  String   |  |  url de logout |
  | attributeReleasePolicy    |     |  |  Stratégie des attributs |


