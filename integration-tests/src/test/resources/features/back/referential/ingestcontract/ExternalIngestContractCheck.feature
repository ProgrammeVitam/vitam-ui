@Api
@ApiReferential
@ApiReferentialIngestContracts
@ApiReferentialIngestContractsCheck
Feature: API contrat d'entrées : vérifier l'existence

  Scenario: d'un contrat d'entrée par son identifiant
    When un utilisateur vérifie l'existence d'un contrat d'entrée par son identifiant
    Then le serveur retourne vrai

