@Api
@ApiIam
@ApiIamCAS
@ApiIamCASSubrogation
Feature: API CAS : Subrogation

  Scenario: Chercher une subrogation par l'email de son superuser
    When un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'email de son superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS
    Then le serveur retourne la bonne subrogation

  Scenario Outline: Chercher une subrogation par l'email de son superuser, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour chercher une subrogation par l'email de son superuser
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur cherche une subrogation par l'email de son superuser
    Then le serveur <autorise ou refuse> l'accès à l'API CAS

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

  Scenario: Chercher une subrogation par l'identifiant de son superuser
    When un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'identifiant de son superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS
    Then le serveur retourne la bonne subrogation

  Scenario: Chercher une subrogation avec un mauvais identifiant de superuser
    When un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation avec un mauvais identifiant de superuser dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS
    Then le serveur retourne un utilisateur non trouvé

  Scenario: Chercher une subrogation par l'identifiant d'un superuser désactivé
    When un utilisateur avec le rôle ROLE_CAS_SUBROGATIONS cherche une subrogation par l'identifiant d'un superuser désactivé dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_SUBROGATIONS
    Then le serveur ne retourne aucune subrogation

  Scenario Outline: Chercher une subrogation par l'identifiant de son superuser, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour chercher une subrogation par l'email de son superuser
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_SUBROGATIONS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur cherche une subrogation par l'identifiant de son superuser
    Then le serveur <autorise ou refuse> l'accès à l'API CAS

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
