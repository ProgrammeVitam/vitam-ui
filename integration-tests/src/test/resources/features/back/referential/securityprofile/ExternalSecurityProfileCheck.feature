@Api
@ApiReferential
@ApiReferentialSecurityProfile
@ApiReferentialSecurityProfileCheck
Feature: API SecurityProfile : vérifier l'existence

  Scenario: d'un profile de sécurité par son identifiant
    When un utilisateur vérifie l'existence d'un profile de sécurité par son identifiant
    Then le serveur retourne vrai
