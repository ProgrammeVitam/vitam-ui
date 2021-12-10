@Api
@ApiIam
@ApiIamProfiles
@ApiIamProfilesCreation
Feature: API Profile : création de profils

	@Traces
  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_PROFILES ajoute un nouveau profil dans un tenant auquel il est autorisé en utilisant un certificat full access avec le rôle ROLE_CREATE_PROFILES
    Then le serveur retourne le profil créé
    And une trace de création de profil est présente dans vitam
