@Api
@ApiIam
@ApiIamUsers
@ApiIamUsersCheck
Feature: API Users : vérifier l'existence

  Scenario: D'un utilisateur
    When un utilisateur avec le rôle ROLE_GET_USERS vérifie l'existence d'un utilisateur par son email dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur retourne vrai

  Scenario: D'un utilisateur par son email mais sans le bon niveau
    When un utilisateur avec le rôle ROLE_GET_USERS sans le bon niveau vérifie l'existence d'un utilisateur par son email dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_USERS
    Then le serveur retourne faux

  Scenario Outline: D'un utilisateur par son email, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Users
    Given deux tenants et un rôle par défaut pour la vérification de l'existence d'un utilisateur par son email
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur vérifie l'existence d'un utilisateur par son email
    Then le serveur <autorise ou refuse> l'accès à l'API Users

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
