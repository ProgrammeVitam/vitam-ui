@Api
@ApiReferential
@ApiReferentialSecurityProfile
@ApiReferentialSecurityProfileCreation
Feature: API SecurityProfile : création d'un nouveau profile de sécurité applicatif

  Scenario: Cas normal
    When un utilisateur avec le rôle ROLE_CREATE_SECURITY_PROFILE ajoute un nouveau profile de sécurité en utilisant un certificat full access avec le rôle ROLE_CREATE_SECURITY_PROFILE
    Then le serveur retourne le profile de sécurité créé
