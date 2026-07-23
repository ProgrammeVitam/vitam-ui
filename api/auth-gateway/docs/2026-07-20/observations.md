Anaylse succinte des modules CAS et IAM:

Les points de blocages forts identifiés:

* utilisation de mécanique métier dans les webflows de CAS (couplage)

Le module IAM porte la base de données avec les droits et les utilisateurs.
La module CAS n'est qu'une sorte d'extention du système pour profiter des mécanismes d'authentification,
gestion des sessions, gestion de password, emailing liés au comptes.
Couplage fort, car utilise customise le webflow pour faire rentré des conceptes métier comme les organisations dans le page du flow.
Sélection des organisations, Subrogation devrait être des processus métier.


Pour découpler il faut faire en sorte le la solution IAM ne se comporte plus que comme un fournisseur d'identité IdP.

L'IdP aura en charge, la création des utilisateurs, gestion de password, otp, sms ...
Le module iam doit prendre en compte uniquement le jeton d'authentification fourni après l'authentification de l'utilisteur.
C'est à partir de ce jeton là que vitam ui va produire un jeton d'accès qui portera les informations métiers comment les droits sur les applications et le reste.

Une fois ce jeton acquis, il pourra utiliser l'application comme un utilisateur normal.


Ce que celà change:

* Comment initialiser le compte super-admin ?

Créer un realm pour le produit.
Créer un compte super-admin dans l'IdP.
Utiliser un script pour definir ce compte comme super admin du système dans la base de données du produit.

A partir d'ici, ce compte pourra créer des organisations qui générera comptes admin et le reste.

* Comment gérer des IdP externes ?

C'est là où est le problème principal, je pense que la bricole dans CAS est liée au fonctionne de base des protocols
SAML et OIDC qui gère l'identity brokering via des redirections uniquement.
Il faut donc choisir l'IdP externe avant de pouvoir s'authentifier.
Celà nécessite que la mire principale permette de choisir l'IdP.
Donc visible de tout le monde.

Garder un découplage complet, c'est sacrifier cette feature.

Si on veut garder la même mécanique qu'actuellement il est fort probable qu'il faille implémenter un SPI pour faire la discovery d'organisations.



