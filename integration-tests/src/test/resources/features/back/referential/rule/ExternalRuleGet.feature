@Api
@ApiReferential
@ApiReferentialContexts
@ApiReferentialContextsGet
Feature: API contextes : récupération des règles

 Scenario: toutes les règles
    When un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then le serveur retourne toutes les règles

 Scenario: une règle par son identifant
    Given la règle RuleTest existe
    When un utilisateur avec le rôle ROLE_GET_RULES récupère une règle par son identifiant en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then le serveur retourne la règle avec cet identifiant

 Scenario: toutes les règle par son intitulé de règle
    Given la règle RuleTest existe
    When un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles par intitulé en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then le serveur retourne toutes les règle avec cet intitulé

 Scenario: toutes les règle par son intitulé de règle
    Given la règle RuleTest existe
    When un utilisateur avec le rôle ROLE_GET_RULES récupère toutes les règles par type en utilisant un certificat full access avec le rôle ROLE_GET_RULES
    Then le serveur retourne toutes les règle avec ce type

 Scenario: toutes les règles avec pagination
    When un utilisateur avec le rôle ROLE_GET_CONTEXTS récupère toutes les règles avec pagination en utilisant un certificat full access avec le rôle ROLE_GET_CONTEXTS
    Then le serveur retourne les règles paginées
