@Api
@ApiIam
@ApiIamGroups
@ApiIamGroupsCheck
Feature: API Groups : vérifier l'existence

  Scenario: D'un groupe par son identifant
    When un utilisateur vérifie l'existence d'un groupe par son identifiant
    Then le serveur retourne vrai

  Scenario: D'un groupe par son nom
    When un utilisateur avec le rôle ROLE_GET_GROUPS vérifie l'existence d'un groupe par son nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS
    Then le serveur retourne vrai

  Scenario: D'un groupe par son nom mais sans le bon niveau
    When un utilisateur avec le rôle ROLE_GET_GROUPS sans le bon niveau vérifie l'existence d'un groupe par son nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_GROUPS
    Then le serveur retourne faux

  Scenario Outline: D'un groupe par son nom, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_GROUPS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api groups
    Given deux tenants et un rôle par défaut pour la vérification de l'existence d'un groupe par son nom
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_GROUPS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_GROUPS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur vérifie l'existence d'un groupe par son nom
    Then le serveur <autorise ou refuse> l'accès à l'API groups

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
