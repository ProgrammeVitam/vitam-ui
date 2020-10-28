@Api
@ApiReferential
@ApiReferentialAccessContracts
@ApiReferentialAccessContractsCheck
Feature: API contrat d'accès : vérifier l'existence

  Scenario: d'un contrat d'accès par son identifiant
    When un utilisateur vérifie l'existence d'un contrat d'accès par son identifiant
    Then le serveur retourne vrai

