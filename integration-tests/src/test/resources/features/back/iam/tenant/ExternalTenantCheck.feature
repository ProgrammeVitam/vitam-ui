@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsCheck
Feature: API Tenant

  Scenario: Vérifier l'existence d'un tenant par son identifant
    When un utilisateur vérifie l'existence d'un tenant par son identifiant
    Then le serveur autorise l'accès à l'API tenants

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_GET_ALL_TENANTS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_GET_ALL_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api tenants
    Given deux tenants et un rôle par défaut pour l'ajout d'un tenant
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_GET_ALL_TENANTS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_GET_ALL_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur vérifie l'existence d'un tenant par son identifiant
    Then le serveur <autorise l'accès à l'API tenants ou refuse l'accès à HEAD tenants>

  Examples:
    | (userRole) avec ou sans | (headerTenant) principal ou secondaire | (certRole) avec ou sans | (certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess | autorise l'accès à l'API tenants ou refuse l'accès à HEAD tenants |
    | avec | principal | avec | sur le tenant principal | autorise l'accès à l'API tenants |
    | sans | principal | avec | sur le tenant principal | refuse l'accès à HEAD tenants |
    | avec | secondaire | avec | sur le tenant principal | refuse l'accès à HEAD tenants |
    | sans | secondaire | avec | sur le tenant principal | refuse l'accès à HEAD tenants |
    | avec | principal | sans | sur le tenant principal | refuse l'accès à HEAD tenants |
    | sans | principal | sans | sur le tenant principal | refuse l'accès à HEAD tenants |
    | avec | secondaire | sans | sur le tenant principal | refuse l'accès à HEAD tenants |
    | sans | secondaire | sans | sur le tenant principal | refuse l'accès à HEAD tenants |
    | avec | principal | avec | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | sans | principal | avec | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | avec | secondaire | avec | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | sans | secondaire | avec | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | avec | principal | sans | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | sans | principal | sans | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | avec | secondaire | sans | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | sans | secondaire | sans | sur le tenant secondaire | refuse l'accès à HEAD tenants |
    | avec | principal | avec | étant fullAccess | autorise l'accès à l'API tenants |
    | sans | principal | avec | étant fullAccess | refuse l'accès à HEAD tenants |
    | avec | secondaire | avec | étant fullAccess | refuse l'accès à HEAD tenants |
    | sans | secondaire | avec | étant fullAccess | refuse l'accès à HEAD tenants |
    | avec | principal | sans | étant fullAccess | refuse l'accès à HEAD tenants |
    | sans | principal | sans | étant fullAccess | refuse l'accès à HEAD tenants |
    | avec | secondaire | sans | étant fullAccess | refuse l'accès à HEAD tenants |
    | sans | secondaire | sans | étant fullAccess | refuse l'accès à HEAD tenants |
