@Api
@ApiIam
@ApiIamProviders
@ApiIamProvidersUpdate
Feature: API provider : mise à jour

	@Traces
  Scenario: Un provider
    Given un provider a été créé
    When un utilisateur met à jour le provider
    Then le serveur refuse la mise à jour car l'opération n'est pas implémentée

  Scenario: Partiellement un provider
    Given un provider a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_PROVIDERS met à jour partiellement un provider dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROVIDERS
    Then le serveur retourne le provider mis à jour
    And une trace de mise à jour du provider est présente dans vitam


	# EDV : it's not allowed to create a readonly provider. You must use a system one
  #Scenario: Partiellement un provider en readonly
    #Given un provider en readonly a été créé
    #When un utilisateur avec le rôle ROLE_UPDATE_PROVIDERS met à jour partiellement un provider en readonly dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_PROVIDERS
    #Then le serveur refuse la mise à jour du provider

  Scenario Outline: Partiellement un provider, cas sécurité, par un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_PROVIDERS sur le tenant principal, après avoir choisi le tenant <(headerTenant) principal ou secondaire> dans l'IHM, et en utilisant un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_PROVIDERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess> = le serveur <autorise ou refuse> l'accès à l'api Providers
    Given un provider a été créé
    And deux tenants et un rôle par défaut pour la mise à jour partielle d'un provider
    And un utilisateur <(userRole) avec ou sans> le rôle ROLE_UPDATE_PROVIDERS sur le tenant principal
    And l'utilisateur a selectionné le tenant <(headerTenant) principal ou secondaire> dans l'IHM
    And un certificat <(certRole) avec ou sans> le rôle ROLE_UPDATE_PROVIDERS <(certTenant) sur le tenant principal ou sur le tenant secondaire ou étant fullAccess>
    When cet utilisateur met à jour partiellement un provider
    Then le serveur <autorise ou refuse> l'accès à l'API Providers

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
