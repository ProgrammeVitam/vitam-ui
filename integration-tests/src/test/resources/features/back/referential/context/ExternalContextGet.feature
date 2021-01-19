@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsGet
Feature: API contextes : récupérer

 Scenario: tous les contextes
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes applicatifs en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne tous les contextes

 Scenario: un contexte par son identifant
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère un contexte par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne le contexte avec cet identifiant

 Scenario: tous les contextes par code et nom
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes par code et nom en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne les contextes par code et nom

 Scenario: tous les contextes par code ou nom
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes par code ou nom en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne les contextes par code ou nom

 Scenario: tous les contextes avec pagination
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère tous les contextes avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne les contexts paginés

 Scenario: l'historique d'un contexte
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère l'historique d'un contexte à partir de son son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne l'historique du contexte




 
