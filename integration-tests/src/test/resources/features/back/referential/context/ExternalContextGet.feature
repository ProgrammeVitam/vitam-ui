@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsGet
Feature: API contextes : recuperer

 Scenario: tous les contextes
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere tous les contextes applicatifs en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne tous les contextes

 Scenario: un contexte par son identifant
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere un contexte par son identifiant en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne le contexte avec cet identifiant

 Scenario: tous les contextes par code et nom
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere tous les contextes par code et nom en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne les contextes par code et nom

 Scenario: tous les contextes par code ou nom
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere tous les contextes par code ou nom en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne les contextes par code ou nom

 Scenario: tous les contextes avec pagination
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere tous les contextes avec pagination en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne les contexts pagines

 Scenario: l'historique d'un contexte
    When un utilisateur avec le role ROLE_GET_CONTEXTS recupere l'historique d'un contexte a partir de son son identifiant en utilisant un certificat full access avec le role ROLE_GET_CONTEXTS
    Then le serveur retourne l'historique du contexte





