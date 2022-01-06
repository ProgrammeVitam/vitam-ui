@Api
@ApiIam
@ApiIamOwners
@ApiIamownersCreation
Feature: API Owner : création d'un nouveau propriétaire

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_OWNERS ajoute un nouveau propriétaire dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_OWNERS
    Then le serveur retourne le propriétaire créé
    And une trace de création d'un propriétaire est présente dans vitam
