@Api
@ApiIam
@ApiIamProviders
@ApiIamProvidersCheck
Feature: Opérations de vérification des identity providers

  Scenario: Vérifier l'existence d'un provider par son identifant
    When un utilisateur vérifie l'existence d'un provider par son identifiant
    Then le serveur refuse la vérification de l'existence car l'opération n'est pas implémentée
