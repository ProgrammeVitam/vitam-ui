## Anonymisation des données personnelles des utilisateurs ##

Dans le cadre des obligations du Règlement Européen sur la Gestion des données personnelles "RGPD", les données personnelles des personnes inactives depuis une période dépassant la durée d'alerte, doivent être supprimées de la base de données d'un système. Il s'agit ici du droit à l'oubli. <br/>
La Partie **RGPD** consiste sur la gestion des utilisateurs VITAMUI, c'est à dire une gestion global (création, modification et suppression des données personnelles).
pour la partie suppression des données personnelles des utilisateurs, on a modifier la partie Modèle des clients et aussi la partie modèle des utilisateur au niveau des projets iam (internal, external) ansi que le projet identity coté Front.

* ### Modification au niveau du modèle  des clients/ Customers :
Pour cela nous avons ajouté deux attributs : 
   - **`gdprAlert`** :  Pour indiquer que le client créé recois ou non une alerte qui indique la présence des utilisateurs inactifs au niveau son organisation.
   - **`gdprAlertDelay` :**  c'est le délais d'alerte pour recevoir une notification qui indique la présence des utilisateurs inactifs à supprimer.
   -  **Attention : ** Ces deux paramètres sont modifiables seulement si le paramètre de configuration `vitamui.iam_internal.gdpr_alert_readonly` est à `false`. Ce paramètre est géré par l'exploitant via le fichier de configuration `environments/group_vars/all/vitamui_vars.yml`. 

* ### Modification au niveau du modèle des utilisateurs  :
Pour cela nous avons ajouté 2 attributs : 
   - Attribut **`DisablingDate`** : pour indiquer la date de désactivation d'un utilisateur.
   - Attribut **`RemovingDate`** : pour indiquer la date de suppression d'un utilisateur

Toutes les données personnelles seront supprimées de la base à part l'email qui sera enregistré mais avec une adresse anonyme sous la forme `anonyme-identifier@nom de l'organisation`
L'action de suppression est irréversible, donc pas de possibilité pour récupérer l'utilisateur / le réactiver ou le modifier après.
