@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsCreation
Feature: API Tenant : Ajouter un nouveau teant

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_TENANTS ajoute un nouveau tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_TENANTS
    Then le serveur retourne le tenant créé
    And 2 profils sont créés pour le tenant
    And une trace de création tenant est présente dans vitam

	Scenario: Cas nom existant du tenant
    When un utilisateur avec le rôle ROLE_CREATE_TENANTS ajoute un tenant avec un nom existant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_TENANTS
    Then le serveur refuse la création du tenant à cause du nom existant

  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_TENANTS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api tenants
    Given deux tenants et un rôle par défaut pour l'ajout d'un tenant
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_TENANTS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_TENANTS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute un nouveau tenant
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
