@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsCreation
Feature: API Subrogations : création

  Scenario: Nouvelle subrogation
   	Given l'utilisateur admin@vitamui.com n'a pas de subrogations en cours
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur retourne la subrogation créée

  Scenario Outline: Nouvelle subrogation, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CREATE_SUBROGATIONS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CREATE_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api subrogations
    Given l'utilisateur testuser@test.com n'a pas de subrogations en cours
    And on demande à ce que le subrogateur soit le user de test
    And deux tenants et un rôle par défaut pour la création d'une subrogation
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CREATE_SUBROGATIONS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CREATE_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute une nouvelle subrogation
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

  Scenario: Ajouter une nouvelle subrogation pour un subrogé non subrogeable
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé non subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur refuse la création de la subrogation à cause de l'utilisateur

  Scenario: Ajouter une nouvelle subrogation pour un subrogé dont le client est non subrogeable
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé dont le client est non subrogeable dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur refuse la création de la subrogation à cause du client

  Scenario: Ajouter une nouvelle subrogation pour un subrogateur non existant
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogateur non existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur refuse la création de la subrogation à cause du subrogateur non existant

  Scenario: Ajouter une nouvelle subrogation pour un subrogé non existant
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé non existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur refuse la création de la subrogation à cause du subrogé non existant

  Scenario: Ajouter une nouvelle subrogation pour un subrogé désactivé
    When un utilisateur avec le rôle ROLE_CREATE_SUBROGATIONS ajoute une nouvelle subrogation pour un subrogé désactivé dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_SUBROGATIONS
    Then le serveur refuse la création de la subrogation à cause du subrogé désactivé
