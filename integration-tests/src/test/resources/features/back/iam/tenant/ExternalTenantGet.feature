@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsGet
Feature: API Tenant

  Scenario: Récupérer tous les tenants
    When un utilisateur récupère tous les tenants
    Then le serveur retourne tous les tenants


  Scenario: Récupérer un tenant par son identifant
    When un utilisateur avec le rôle ROLE_GET_ALL_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS
    Then le serveur retourne le tenant avec cet identifiant

  Scenario: Récupérer un tenant par son identifant par un utilisateur avec le rôle ROLE_GET_TENANTS et dans le même tenant
    When un utilisateur avec le rôle ROLE_GET_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé et identique au tenant récupéré en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS
    Then le serveur retourne le tenant avec cet identifiant

  Scenario: Récupérer un tenant par son identifant par un utilisateur avec le rôle ROLE_GET_TENANTS et dans un tenant différent
    When un utilisateur avec le rôle ROLE_GET_TENANTS récupère un tenant par son identifiant dans un tenant auquel il est autorisé mais différent du tenant récupéré en utilisant un certificat full access avec le rôle ROLE_GET_ALL_TENANTS
    Then le serveur retourne aucun tenant

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_TENANTS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_GET_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api CAS
    Given deux tenants et un rôle par défaut pour la récupération d'un tenant par son identifiant
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_GET_TENANTS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_GET_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur récupère un tenant par son identifiant
    Then le serveur <autorise ou refuse> l'accès à l'API tenants

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


  Scenario: Récupérer tous les tenants par le nom
    When un utilisateur avec le rôle ROLE_GET_TENANTS récupère tous les tenants par le nom dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_GET_TENANTS
    Then le serveur retourne tous les tenants avec ce nom

  Scenario: Récupérer tous les tenants par le nom par un utilisateur qui n'est pas autorisé sur le tenant
    When un utilisateur avec le rôle ROLE_GET_TENANTS récupère tous les tenants par le nom dans un tenant auquel il n'est pas autorisé en utilisant un certificat full access avec le rôle ROLE_GET_TENANTS
    Then le serveur refuse l'accès à l'API tenants
