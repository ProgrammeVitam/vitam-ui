@Api
@ApiIam
@ApiIamSubrogations
@ApiIamSubrogationsUpdate
Feature: Opérations de mise à jour des subrogations

  Scenario: Mettre à jour une subrogation
    Given une subrogation a été créée
    When un utilisateur met à jour une subrogation
    Then le serveur refuse la mise à jour car l'opération n'est pas implémentée
