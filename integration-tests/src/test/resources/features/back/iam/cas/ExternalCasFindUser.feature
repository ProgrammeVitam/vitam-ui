@Api
@ApiIam
@ApiIamCAS
@ApiIamCASFind
Feature: API CAS : Chercher

  Scenario: Un utilisateur par son email en demandant un token d'authentification
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur par son email en demandant un token d'authentification dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne le bon utilisateur avec son token d'authentification

  Scenario: Un utilisateur inexistant par son email
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur inexistant par son email dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne un utilisateur non trouvé

  Scenario: Un utilisateur désactivé par son email en demandant un token d'authentification
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur désactivé par son email en demandant un token d'authentification dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne un utilisateur indisponible

  Scenario Outline: Un utilisateur par son email, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour la recherche d'un utilisateur par email
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur cherche un utilisateur par son email
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


  Scenario: Un utilisateur par son identifiant
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne le bon utilisateur

  Scenario: Un utilisateur inexistant par son identifiant
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur inexistant par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne un utilisateur non trouvé

  Scenario: Un utilisateur désactivé par son identifiant
    When un utilisateur avec le rôle ROLE_CAS_USERS cherche un utilisateur désactivé par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat sur le tenant et avec le rôle ROLE_CAS_USERS
    Then le serveur retourne un utilisateur indisponible

  Scenario Outline: Un utilisateur par son identifiant, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_USERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour la recherche d'un utilisateur par identifiant
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_CAS_USERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_CAS_USERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur cherche un utilisateur par son identifiant
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
