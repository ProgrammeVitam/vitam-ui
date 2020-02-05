@Api
@ApiIam
@ApiIamProviders
@ApiIamProvidersDelete
Feature: Opérations de suppresion des identity providers

  Scenario: Supprimer un provider
    Given un provider a été créé
    When un utilisateur supprime le provider
    Then le serveur refuse la suppression car l'opération n'est pas implémentée
