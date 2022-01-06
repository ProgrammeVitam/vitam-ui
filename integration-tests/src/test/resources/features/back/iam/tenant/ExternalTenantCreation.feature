@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsCreation
Feature: API Tenant : Ajouter un nouveau teant

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_TENANTS ajoute un nouveau tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_TENANTS
    Then le serveur retourne le tenant créé
    And 15 profils sont créés pour le tenant
    And une trace de création tenant est présente dans vitam
