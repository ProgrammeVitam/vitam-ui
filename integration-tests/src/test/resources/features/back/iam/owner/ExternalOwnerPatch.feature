@Api
@ApiIam
@ApiIamOwners
@ApiIamOwnersPatch
Feature: API Owners : mise à jour partielle d'un propriétaire

	@Traces
  Scenario: Cas normal
    Given un propriétaire a été créé
    When un utilisateur avec le rôle ROLE_UPDATE_OWNERS met à jour partiellement un propriétaire dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_UPDATE_OWNERS
    Then le serveur retourne le propriétaire partiellement mis à jour
    And une trace de mise à jour du propriétaire est présente dans vitam
