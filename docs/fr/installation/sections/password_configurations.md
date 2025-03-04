# Configuration des profils des mots de passe (Ansiblerie)

La configuration de la complexité des mots de passe est externalisé du serveur CAS, la configuration actuelle est basée sur des profils de configurations.

L'intégrateur de VITAMUI, peut choisir le profil de configuration personnalisé par instance.

Pour répondre aux exigeances de la complexité des mots de passes de l'ANSSI (Agence Nationale de la Sécurité des Systèmes d'Informations), un profil dédié est configuré par défaut.

Ce profil est nommé `anssi`, l'exploitant peut le changer en choisissant le profil custom, qui garde les anciens comportements.

## Configuration pour le profil personnalisé (profil custom)

Exemple de profil custom pouvant être surchargé dans le fichier `environments/vitamui_extra_vars.yml`.

```yaml
# Custom password configuration
vitamui_password_configurations:
  customPolicyPattern: '^(?=.*[$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`])(?=.*[a-z])(?=.*[A-Z])(?=.*[\d])[A-Za-zÀ-ÿ0-9$@!%*#£?&=\-\/:;\(\)"\.,\?!''\[\]{}^\+\=_\\\|~<>`]{${password.length},}$'
  password:
    profile: "custom"
    length: 8
    max_old_password: 3
    check_occurence: false
    constraints:
      customs:
        fr:
          title: 'Pour des raisons de sécurité, votre mot de passe doit:'
          messages:
            - Au moins ${password.length} caractères
            - Des minuscules et des majuscules
            - Au moins un chiffre et un caractère spécial
            - Etre différent des ${password.max-old-password} derniers mots de passe
        en:
          title: 'For security reasons, your password must:'
          messages:
            - At least ${password.length} characters
            - Lowercase and uppercase
            - At least one number and one special character
            - Be different from the last ${password.max-old-password} passwords
        de:
          title: 'Aus Sicherheitsgründen muss Ihr Passwort:'
          messages:
            - Mindestens ${password.length} Zeichen
            - Klein- und Großbuchstaben
            - Mindestens eine Zahl und ein Sonderzeichen
            - Unterscheiden Sie sich von den letzten ${password.max-old-password} Passwörtern
```

### Explication de la configuration

#### Configuration communes

* `vitamui_password_configurations` est le bloc racine, qui se situe dans le fichier `vitamui_vars.yml`

* `anssiPolicyPattern`: L'expression régulière pour le profil ANSSI.

* `customPolicyPattern`: L'expression régulière pour le profil personnalisé (custom).

> L'expression régulière qui sera utilisé, dépendra du profil choisi.

* `password`: Le préfixe de configuration qui sera chargé dans le fichier de configuration pricipale du serveur CAS.

* `profile`: Le nom du profil à utiliser (par défault `anssi`), ou bien `custom`, pour éviter des erreurs de configuration au chargement du serveur, La déclaration d'un nom de profil devrait etre cohérent avec le bloc de configuration adéquat (voir explicaion de ces blocs au dessous).

* `length`: La taille du mot de passe (par défaut 12 pour le profil anssi, 8 pour le profil personnalisé).

* `max_old_password`: Le nombre de mots de passe anciens à ne pas réutiliser (par défaut 12 pour le profil anssi, 3 pour le profil custom).

* `check_occurrence`: Le boolean permettant de vérifier la présence des occurrences du nom d'utilisateur dans le mot de passe (par défaut à `true` pour le profil anssi, `false` ou absent pour le profil custom).

* `occurrences_chars_number`: Le nombre de caractères issues du nom d'utilisateur tolérables à utiliser dans le mot de passe (par défaut à `3` pour le profil anssi, `0` ou absent pour le profil custom).

* `constraints`: bloc des différentes contraintes des mots de passe par profile.

#### Configuration pour le profil ANSSI

Le sous bloc `defaults` du bloc `constraints` concerne les configurations par défault par bloc de langue.

Ce bloc contient la liste des messages personnalisés, et les différentes contraintes en termes des caractères alphanumérique, spéciaux, miniscules, majuscules etc..

#### Configuration pour le profil personnalisé

Le sous bloc `customs` du bloc `constraints` concerne les configurations du profil personnalisé par bloc de langue.

Ce bloc contient la liste des messages personnalisés, et les différentes contraintes en termes des caractères alphanumérique, spéciaux, miniscules, majuscules etc..

> Note:
en cas de changement manuelle par l'administrateur système du nombre de mots passe anciens à utiliser, le changement devra se faire au niveau CAS et iam.
> Le redémarrage de ces deux composants est nécessaire.
>
> La modification des contraintes d'authentification est transparente pour les utilisateurs qui possèdent déjà des comptes dans Vitam-UI.
> Ces nouvelles containtes seront appliquées lors du changement de mots de passe suite à expiration de celui ci.

Voir le document d'exploitation qui contient différents exemples de configurations par profil.

#### Les langues supportées par CAS à ce jour

Les langues supportées par CAS sont: le Français, l'Anglais et l'Allemand.

Il est fortement recommandé de définir les trois blocs des différentes langues, pour garder la cohérence avec les différentes interfaces du serveur d'authentification CAS.
