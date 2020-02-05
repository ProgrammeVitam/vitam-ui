@Api
@ApiIam
@ApiIamTenants
@ApiIamTenantsPatch
Feature: API Tenants : mise à jour partielle d'un tenant

	@Traces
  Scenario: Cas normal
    Given un tenant a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_TENANTS met à jour partiellement un tenant dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_TENANTS
    Then le serveur retourne le tenant partiellement mis à jour
    And une trace de mise à jour du tenant est présente dans vitam
