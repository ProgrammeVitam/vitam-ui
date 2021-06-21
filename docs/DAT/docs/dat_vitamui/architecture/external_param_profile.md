
## Profils de paramétrage externes 

### External Parameter Profile

Un profil de paramétrage externe est une entité fictive, contient les informations suivantes :

* le nom du profil (nom)
* la description du profil (description)
* le contrat d'accès associé (accessContract: voir external parameters)
* le statut du profil (enabled)

Un profil de paramétrage externe permet d'associer un et unique profil à un contrat d'accès qui est lui meme lié à un paramétrage externe (ExternalParameters).

### Profil  

voir [section 3.8](3.8)

### External Parameters

TODO voir section concernée.

### Illustration

Donnée du profil

```
{
    "_id": "60d06c74663b6f71e8459eb0168d408ea49743f8bc4f80f21f3eeb266ec90cca",
    "identifier": "216",
    "name": "test profile",
    "enabled": true,
    "description": "test description profile",
    "tenantIdentifier": 1,
    "applicationName": "EXTERNAL_PARAM_PROFILE_APP",
    "roles": [
        {
            "name": "ROLE_CREATE_EXTERNAL_PARAM_PROFILE"
        },
        {
            "name": "ROLE_EDIT_EXTERNAL_PARAM_PROFILE"
        },
        {
            "name": "ROLE_SEARCH_EXTERNAL_PARAM_PROFILE"
        }
    ],
    "level": "",
    "readonly": false,
    "externalParamId": "reference_identifier",
    "customerId": "system_customer",
    "_class": "profiles"
}
```
      
Donnée de l'external parameter

```
{
    "_id": "60d06c73663b6f71e8459eae3ce591e616a1428bb086acd40c5c517eb8ccfda7",
    "identifier": "reference_identifier",
    "name": "test profile",
    "parameters": [
        {
            "key": "PARAM_ACCESS_CONTRACT",
            "value": "ContratTNR"
        }
    ],
    "_class": "externalParameters"
}
```

Le profil de paramétrage externe provenant des deux données ci-dessus

```
{
  "id": "60d06c74663b6f71e8459eb0168d408ea49743f8bc4f80f21f3eeb266ec90cca",
  "name": "test profile",
  "description": "test description profile",
  "accessContract": "ContratTNR",
  "profileIdentifier": "216",
  "idProfile": "60d06c74663b6f71e8459eb0168d408ea49743f8bc4f80f21f3eeb266ec90cca",
  "externalParamIdentifier": "reference_identifier",
  "idExternalParam": "60d06c73663b6f71e8459eae3ce591e616a1428bb086acd40c5c517eb8ccfda7",
  "enabled": true,
  "dateTime": "2021-06-21T12:52:34.430803Z"
}
```

### Événement lors de la mise à jour

La mise à jour du profil de paramétrage externe peut générer jusqu'à trois événements de journalisations.

Premier cas:

* Modification des données liés aux données du profil
  * Dans ce cas de figure, on émet un événement de journal externe de type `EXT_VITAMUI_UPDATE_PROFILE`.
  * et un événement de modification du profil de paramétrage externe `EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE`

Deuxième cas:

* Modification des données liés à la donnée du paramétrage externe
  * Dans ce cas de figure, on émet un événement de journal externe de type `EXT_VITAMUI_UPDATE_EXTERNAL_PARAM`.
  * et un événement de modification du profil de paramétrage externe `EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE`.


Troisième cas:

* Modification des données liés aux données du profil et du paramétrage externe, dans ce cas de figure, on émet 3 événements de journalisation :
  * événement de type `EXT_VITAMUI_UPDATE_PROFILE`.
  * événement de type `EXT_VITAMUI_UPDATE_EXTERNAL_PARAM`.
  * et un événement de modification du profil de paramétrage externe `EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE`.


Exemple de mise à jour de la description du profil:

```
{
    "_id": "aecaaaaaaghohlrwaan3ial2fyt7xnaaaaaq",
    "tenantIdentifier": 1,
    "accessContractLogbookIdentifier": "AC-000002",
    "evType": "EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE",
    "evTypeProc": "EXTERNAL_LOGBOOK",
    "outcome": "OK",
    "outMessg": "Le profil paramètrage externe a été modifié",
    "outDetail": "EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE.OK",
    "evIdReq": "5043309c-c8d7-4bc9-bfcc-bd20852ce90e",
    "evDateTime": "2021-06-21T10:40:10.164438Z",
    "obId": "216",
    "obIdReq": "externalparamprofile",
    "evDetData": "{\"diff\":{\"-Description\":\"test description profile\",\"+Description\":\"test description profile updated\"}}",
    "evIdAppSession": "EXTERNAL_PARAM_PROFILE_APP63597049221:5043309c-c8d7-4bc9-bfcc-bd20852ce90e:Contexte UI Identity:1:-:1",
    "creationDate": 3960212756529822,
    "status": "SUCCESS",
    "_class": "events",
    "synchronizedVitamDate": "2021-06-21T10:40:36.370328Z",
    "vitamResponse": "{\"httpCode\":201,\"code\":\"\"}"
}
```
