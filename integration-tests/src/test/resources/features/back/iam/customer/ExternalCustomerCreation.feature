@Api
@ApiIam
@ApiIamCustomers
@ApiIamCustomersCreation
Feature: API Customers : création d'un nouveau client

  @Traces
  @ApiIamCustomersCreationWithoutLogo
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS
    Then le serveur retourne le client créé
    And un identity provider par défaut est associé au client
    And un tenant par défaut est créé
    And 6 profils sont créés pour le tenant principal
    And un utilisateur admin est associé au client
    And une trace de création d'un client est présente dans vitam


  @ApiIamCustomersCreationLogo
  Scenario: Cas normal avec un logo
    When un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client avec un thème personnalisé dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS
    Then le serveur retourne le client créé
    And un identity provider par défaut est associé au client
    And un tenant par défaut est créé
    And 6 profils sont créés pour le tenant principal
    And un utilisateur admin est associé au client
    And une trace de création d'un client est présente dans vitam

    @ApiIamCustomersCreationWithTheme
    Scenario: Cas normal avec un logo
        When un utilisateur avec le rôle ROLE_CREATE_CUSTOMERS ajoute un nouveau client avec son logo dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_CUSTOMERS
        Then le serveur retourne le client créé
        And un identity provider par défaut est associé au client
        And un tenant par défaut est créé
        And 6 profils sont créés pour le tenant principal
        And un utilisateur admin est associé au client
        And une trace de création d'un client est présente dans vitam


  Scenario Outline: Cas sécurité, par un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_CUSTOMERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_CUSTOMERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api customers
    Given deux tenants et un rôle par défaut pour l'ajout d'un client
    And un utilisateur <(userRole) avec ou sans> les rôles ROLE_CREATE_CUSTOMERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> les rôles ROLE_CREATE_CUSTOMERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur ajoute un nouveau client
    Then le serveur <autorise ou refuse> l'accès à l'API customers

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
