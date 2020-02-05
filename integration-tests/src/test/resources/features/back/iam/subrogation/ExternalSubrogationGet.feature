@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsGet
Feature: API Subrogation : récupération

  Scenario Outline: toutes les subrogations, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api subrogations
    Given deux tenants et un rôle par défaut pour la récupération d'une subrogation par son identifiant
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur récupère toutes les subrogations
    Then le serveur <autorise ou refuse> l'accès à l'API subrogations

  Examples:
    | (userRole) avec ou sans | (headerTenant) principal ou secondaire | (certRole) avec ou sans | (certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess | autorise ou refuse |
    | avec | principal | avec | sur le tenant principal | autorise |
    | sans | principal | avec | sur le tenant principal | refuse |
    | avec | secondaire | avec | sur le tenant principal | refuse |
    | sans | secondaire | avec | sur le tenant principal | refuse |
    | avec | principal | sans | sur le tenant principal | refuse |
    | sans | principal | sans | sur le tenant principal | refuse |
    | avec | secondaire | sans | sur le tenant principal | refuse |
    | sans | secondaire | sans | sur le tenant principal | refuse |
    | avec | principal | avec | sur le tenant secondaire | refuse |
    | sans | principal | avec | sur le tenant secondaire | refuse |
    | avec | secondaire | avec | sur le tenant secondaire | refuse |
    | sans | secondaire | avec | sur le tenant secondaire | refuse |
    | avec | principal | sans | sur le tenant secondaire | refuse |
    | sans | principal | sans | sur le tenant secondaire | refuse |
    | avec | secondaire | sans | sur le tenant secondaire | refuse |
    | sans | secondaire | sans | sur le tenant secondaire | refuse |
    | avec | principal | avec | étant fullAccess | autorise |
    | sans | principal | avec | étant fullAccess | refuse |
    | avec | secondaire | avec | étant fullAccess | refuse |
    | sans | secondaire | avec | étant fullAccess | refuse |
    | avec | principal | sans | étant fullAccess | refuse |
    | sans | principal | sans | étant fullAccess | refuse |
    | avec | secondaire | sans | étant fullAccess | refuse |
    | sans | secondaire | sans | étant fullAccess | refuse |


  Scenario: Une subrogation par son identifant
    When un utilisateur avec le rôle ROLE_GET_SUBROGATIONS récupère une subrogation par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_SUBROGATIONS
    Then le serveur retourne la subrogation avec cet identifiant

  Scenario Outline: Une subrogation par son identifant, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api subrogations
    Given deux tenants et un rôle par défaut pour la récupération d'une subrogation par son identifiant
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur récupère une subrogation par son identifiant
    Then le serveur <autorise ou refuse> l'accès à l'API subrogations

  Examples:
    | (userRole) avec ou sans | (headerTenant) principal ou secondaire | (certRole) avec ou sans | (certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess | autorise ou refuse |
    | avec | principal | avec | sur le tenant principal | autorise |
    | sans | principal | avec | sur le tenant principal | refuse |
    | avec | secondaire | avec | sur le tenant principal | refuse |
    | sans | secondaire | avec | sur le tenant principal | refuse |
    | avec | principal | sans | sur le tenant principal | refuse |
    | sans | principal | sans | sur le tenant principal | refuse |
    | avec | secondaire | sans | sur le tenant principal | refuse |
    | sans | secondaire | sans | sur le tenant principal | refuse |
    | avec | principal | avec | sur le tenant secondaire | refuse |
    | sans | principal | avec | sur le tenant secondaire | refuse |
    | avec | secondaire | avec | sur le tenant secondaire | refuse |
    | sans | secondaire | avec | sur le tenant secondaire | refuse |
    | avec | principal | sans | sur le tenant secondaire | refuse |
    | sans | principal | sans | sur le tenant secondaire | refuse |
    | avec | secondaire | sans | sur le tenant secondaire | refuse |
    | sans | secondaire | sans | sur le tenant secondaire | refuse |
    | avec | principal | avec | étant fullAccess | autorise |
    | sans | principal | avec | étant fullAccess | refuse |
    | avec | secondaire | avec | étant fullAccess | refuse |
    | sans | secondaire | avec | étant fullAccess | refuse |
    | avec | principal | sans | étant fullAccess | refuse |
    | sans | principal | sans | étant fullAccess | refuse |
    | avec | secondaire | sans | étant fullAccess | refuse |
    | sans | secondaire | sans | étant fullAccess | refuse |


  Scenario: Retrouver ma subrogation en tant que subrogé
    Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    And une subrogation existe pour moi en tant que subrogé
    When je demande ma subrogation courante en tant que subrogé
    Then le serveur retourne la subrogation qui existe pour moi en tant que subrogé

  Scenario: Ne pas retrouver de subrogation en tant que subrogé
    Given aucune subrogation n'existe pour moi en tant que subrogé
    When je demande ma subrogation courante en tant que subrogé
    Then le serveur ne retourne aucune subrogation pour moi en tant que subrogé

  Scenario: Retrouver ma subrogation en tant que subrogateur
    Given une subrogation existe pour moi en tant que subrogateur
    When je demande ma subrogation courante en tant que subrogateur
    Then le serveur retourne la subrogation qui existe pour moi en tant que subrogateur

  Scenario: Ne pas retrouver de subrogation en tant que subrogateur
    Given aucune subrogation n'existe pour moi en tant que subrogateur
    When je demande ma subrogation courante en tant que subrogateur
    Then le serveur ne retourne aucune subrogation pour moi en tant que subrogateur
