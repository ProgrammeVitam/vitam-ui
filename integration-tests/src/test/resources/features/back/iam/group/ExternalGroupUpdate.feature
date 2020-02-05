@Api
@ApiIam
@ApiIamGroups
@ApiIamGroupsUpdate
Feature: API Groups : mise à jour d'un groupe

  Scenario: Opération non implémentée
    Given un groupe a été créé
    When un utilisateur met à jour un groupe
    Then le serveur refuse la mise à jour car l'opération n'est pas implémentée
